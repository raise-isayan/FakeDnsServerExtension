package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.extension.ExtensionUnloadingHandler;
import burp.api.montoya.persistence.Preferences;
import extension.burp.BurpExtensionImpl;
import extension.burp.IPropertyConfig;
import fakedns.model.FakeDnsProperty;
import fakedns.model.OptionProperty;
import fakedns.server.SimpleDnsServer;
import fakedns.view.FakeDnsTab;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author isayan
 */
public class BurpExtension extends BurpExtensionImpl implements ExtensionUnloadingHandler {

    private final static Logger logger = Logger.getLogger(BurpExtension.class.getName());

    private final static java.util.ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("burp/resources/release");

    private SimpleDnsServer server = null;

    @Override
    public void initialize(MontoyaApi api) {
        super.initialize(api);
        api.extension().setName(BUNDLE.getString("projname"));

        // 設定ファイル読み込み
        IPropertyConfig config = this.properyFakeDns;
        if (config != null) {
            Map<String, String> settings = this.option.loadConfigSetting();
            Preferences pref = api.persistence().preferences();
            String value = pref.getString(config.getSettingName());
            settings.put(config.getSettingName(), value == null ? config.defaultSetting() : value);
            config.saveSetting(settings.get(config.getSettingName()));
            this.tabFakeDnsTab.getUiComponent().addPropertyChangeListener(config.getSettingName(), newPropertyChangeListener());
        }

        this.server = new SimpleDnsServer(api);
        this.tabFakeDnsTab.setProperty(this.properyFakeDns);
        api.userInterface().registerSuiteTab(this.tabFakeDnsTab.getTabCaption(), this.tabFakeDnsTab.getUiComponent());
        api.extension().registerUnloadingHandler(this);
    }

    public PropertyChangeListener newPropertyChangeListener() {
        return new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                IPropertyConfig config = tabFakeDnsTab.getProperty();
                if (config != null) {
                    if (config.getSettingName().equals(evt.getPropertyName())) {
                        Map<String, String> settings = option.loadConfigSetting();
                        settings.put(config.getSettingName(), config.loadSetting());
                        applyOptionProperty();
                    }
                }
            }
        };
    }

    public SimpleDnsServer getServer() {
        return this.server;
    }

    private void applyOptionProperty() {
        Map<String, String> settings = this.option.loadConfigSetting();
        Preferences pref = api().persistence().preferences();
        for (String key : settings.keySet()) {
            pref.setString(key, settings.get(key));
        }

    }

    private final FakeDnsTab tabFakeDnsTab = new FakeDnsTab();
    private final FakeDnsProperty properyFakeDns = new FakeDnsProperty();

    final OptionProperty option = new OptionProperty();

    public OptionProperty getProperty() {
        return this.option;
    }

    @SuppressWarnings("unchecked")
    public static BurpExtension getInstance() {
        return BurpExtensionImpl.<BurpExtension>getInstance();
    }

    @Override
    public void extensionUnloaded() {
        if (this.server != null && this.server.isRunning()) {
            this.server.stopServer();
        }
        this.applyOptionProperty();
    }

}
