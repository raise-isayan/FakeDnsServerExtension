package fakedns.server;

import burp.BurpPreferences;
import burp.api.montoya.persistence.Preferences;
import extension.burp.BurpConfig;
import extension.burp.HostName;
import extension.burp.HostNameEntry;
import extension.helpers.ConvertUtil;
import extension.helpers.IpUtil;
import extension.helpers.StringUtil;
import fakedns.model.HostNameItem;
import fakedns.model.FakeDnsProperty;
import fakedns.view.FakeDnsTab;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

/**
 *
 * @author isayan
 */
public class SimpleDnsServer {

    private final static Logger logger = Logger.getLogger(SimpleDnsServer.class.getName());

    private final static java.util.ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("burp/resources/release");

    private static void usage() {
        System.out.println("");
        System.out.println(String.format("Usage: java -jar %s.jar [option] [-i <interface>] [--fakeip <fakeip>] [--fakedomains <FakeDomains>] [--nameservers <NameServers>] [--dnsport <dnPport>]", RELEASE.getString("projname")));
        System.out.println("[option]");
        System.out.println("\t-h - help show");
        System.out.println("\t-gui - GUI Mode ");
        System.out.println("[command]");
        System.out.println("\t-i <interface> - Specify the interface.");
        System.out.println("\t--fakeip <fakeip> - Specify the IP address to spoof");
        System.out.println("\t--fakedomains <FakeDomains> - Specify the domain to spoof");
        System.out.println("\t--nameservers <NameServers>  - Specify the name server");
        System.out.println("\t--port <dnsPort> - Specify the DNS port");
        System.out.println("");
    }

    private final static java.util.ResourceBundle RELEASE = java.util.ResourceBundle.getBundle("burp/resources/release");

    public static String getProjectName() {
        return RELEASE.getString("projname");
    }

    public static String getVersion() {
        return RELEASE.getString("version");
    }

    /*
        args = new String[]{"-gui"};
        args = new String[]{"-i","192.168.137.1", "--fakeip", "192.168.137.1", "--fakedomains", "www.example.com,www.example.jp"};
    */
    public static void main(String[] args) {
        final FakeDnsProperty fakeDnsOption = new FakeDnsProperty();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                // --- 引数なしのオプション ---
                case "-v": {
                    System.out.println("Version: " + getVersion());
                    System.out.println("Language: " + Locale.getDefault().getLanguage());
                    return;
                }
                case "-h": {
                    usage();
                    return;
                }
                case "-gui": {
                    EventQueue.invokeLater(MainPanel::createAndShowGui);
                    return;
                }
                // --- 引数ありのオプション ---
                case "-i": {
                    if (i + 1 < args.length) {
                        fakeDnsOption.setBindInterface(args[++i]);
                    } else {
                        System.err.println(args[++i] + " requires arguments");
                        return;
                    }
                    break;
                }
                case "--fakeip": {
                    if (i + 1 < args.length) {
                        String fakeIP = args[++i];
                        fakeDnsOption.setFakeIP(fakeIP);
                    } else {
                        System.err.println(args[++i] + " requires arguments");
                        return;
                    }
                    break;
                }
                case "--fakedomains": {
                    if (i + 1 < args.length) {
                        String fakeDomains = args[++i];
                        fakeDnsOption.setFakeDomains(HostNameItem.parseHostList(fakeDomains));
                    } else {
                        System.err.println(args[++i] + " requires arguments");
                        return;
                    }
                    break;
                }
                case "--nameservers": {
                    if (i + 1 < args.length) {
                        String nameservers = args[++i];
                        fakeDnsOption.setNameServers(HostNameItem.parseHostList(nameservers));
                    } else {
                        System.err.println(args[++i] + " requires arguments");
                        return;
                    }
                    break;
                }
                case "-p":
                case "--port": {
                    if (i + 1 < args.length) {
                        int dnsPort = ConvertUtil.parseIntDefault(args[++i], 53);
                        fakeDnsOption.setDnsPort(dnsPort);
                    } else {
                        System.err.println(args[++i] + " requires arguments");
                        return;
                    }
                    break;
                }
                default: {
                    System.err.println("Unknown optoin: " + args[i]);
                    usage();
                    return;
                }
            }

        }

        // インタフェース
        if (!IpUtil.isIPv4Address(fakeDnsOption.getBindInterface())) {
            System.out.println("-i ipv4 format error:" + fakeDnsOption.getBindInterface());
            usage();
            return;
        }

        // 偽装IP
        if (!IpUtil.isIPv4Address(fakeDnsOption.getFakeIP())) {
            System.out.println("--fakeip ipv4 format error:" + fakeDnsOption.getFakeIP());
            usage();
            return;
        }

        System.out.println("Bind intarface:" + fakeDnsOption.getBindInterface());
        System.out.println("FakeIP:" + fakeDnsOption.getFakeIP());
        System.out.println("port:" + fakeDnsOption.getDnsPort());
        if (!fakeDnsOption.getFakeDomains().isEmpty()) {
            System.out.println("FakeDomains:" + StringUtil.join(",", HostNameItem.toStringArray(fakeDnsOption.getFakeDomains())));
        }
        if (!fakeDnsOption.getNameServers().isEmpty()) {
            System.out.println("NameServers:" + StringUtil.join(",", HostNameItem.toStringArray(fakeDnsOption.getNameServers())));
        }
        // ハンドラー（Runnable）を作成
        SimpleDnsServer dnsServer = new SimpleDnsServer();
        dnsServer.setFakeDnsOption(fakeDnsOption);
        dnsServer.setEventHandler(new DnsHandler.MessageHandler() {

            @Override
            public void message(String message) {
                System.out.println(message);
            }

            @Override
            public void catchException(Thread t, Throwable ex) {
                String message = "";
                if (ex instanceof BindException) {
                    message = "Bind Error: " + fakeDnsOption.getBindInterface() + " - " + ex.getMessage();
                    System.out.println("Bind Error: " + StringUtil.getStackTrace(ex.getMessage(), ex));
                } else if (ex instanceof IOException) {
                    message = "Fatal Error: " + StringUtil.getStackTrace(ex.getMessage(), ex);
                }
                System.out.println(message);
            }

        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            dnsServer.stopServer();
        }));
        dnsServer.startServer();
        System.out.println("Main thread: DNS Server thread started.");
        dnsServer.joinServer();
        System.out.println("Main thread terminate.");
    }

    private final burp.api.montoya.MontoyaApi api;
    private final FakeDnsProperty fakeDnsOption = new FakeDnsProperty();

    /**
     * @return the fakeDnsOption
     */
    public FakeDnsProperty getFakeDnsOption() {
        return this.fakeDnsOption;
    }

    /**
     * @param fakeDnsOption the fakeDnsOption to set
     */
    public void setFakeDnsOption(FakeDnsProperty fakeDnsOption) {
        this.fakeDnsOption.setProperty(fakeDnsOption);
    }

    // private DnsHandler dnsHandler = null;
    private Thread dnsServer = null;

    /**
     *
     */
    public SimpleDnsServer() {
        this.api = null;
    }

    public SimpleDnsServer(burp.api.montoya.MontoyaApi api) {
        this.api = api;
    }

    public synchronized void startServer() {
        List<HostNameEntry> burpHostList = new ArrayList<>();
        if (this.api != null) {
            List<BurpConfig.HostnameResolution> burp_dns_resolv = BurpConfig.getHostnameResolution(this.api);
            for (BurpConfig.HostnameResolution hostEntry : burp_dns_resolv) {
                if (hostEntry.isEnabled()) {
                    burpHostList.add(new HostNameEntry(hostEntry.getIPAddress(), hostEntry.getHostname()));
                }
            }
        }
        this.getFakeDnsOption().setBurpHosts(HostName.getInstance(burpHostList));
        DnsHandler dnsHandler = new DnsHandler(this.getFakeDnsOption());
        dnsHandler.setExceptionHandler(this.getExceptionHandler());

        // スレッドを作成して開始
        this.dnsServer = new Thread(dnsHandler);
//        dnsThread.setDaemon(false); // メインが終了しても動き続ける（必要に応じて設定）
        this.dnsServer.start();

    }

    public synchronized boolean isRunning() {
        return (this.dnsServer != null);
    }

    public void joinServer() {
        if (this.dnsServer != null) {
            try {
                this.dnsServer.join();
            } catch (InterruptedException ex) {
                stopServer();
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    public synchronized void stopServer() {
        if (this.dnsServer != null) {
            this.dnsServer.interrupt();
        }
        this.dnsServer = null;
    }


    private DnsHandler.MessageHandler messageHandler = null;

    public DnsHandler.MessageHandler getExceptionHandler() {
        return this.messageHandler;
    }

    public void setEventHandler(DnsHandler.MessageHandler exceptionHandler) {
        this.messageHandler = exceptionHandler;
    }


    private final static class MainPanel extends JPanel {

        private final FakeDnsTab fakeDnsTab = new FakeDnsTab();

        private MainPanel() {
            super(new BorderLayout());
            Preferences pref = BurpPreferences.extensions(getProjectName());
            FakeDnsProperty option = new FakeDnsProperty();
            option.saveSetting(pref.getString(option.getSettingName()));
            this.fakeDnsTab.setProperty(option);
            this.add(this.fakeDnsTab, BorderLayout.CENTER);
            setPreferredSize(new Dimension(800, 800));
        }

        private static void createAndShowGui() {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                JFrame frame = new JFrame(SimpleDnsServer.getProjectName());
                MainPanel main = new MainPanel();
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.getContentPane().add(main);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        FakeDnsProperty option = main.fakeDnsTab.getProperty();
                        Preferences pref = BurpPreferences.extensions(getProjectName());
                        pref.setString(option.getSettingName(), option.loadSetting());
                    }
                });
            } catch (UnsupportedLookAndFeelException ignored) {
                Toolkit.getDefaultToolkit().beep();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                System.err.println(StringUtil.getStackTrace(ex.getMessage(), ex));
            }
        }
    }

}
