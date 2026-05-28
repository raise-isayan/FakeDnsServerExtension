package fakedns.server;

import burp.BurpPreferences;
import burp.api.montoya.persistence.Preferences;
import extend.util.external.NetUtil;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.SocketException;
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

    private final static java.util.ResourceBundle RELEASE = java.util.ResourceBundle.getBundle("burp/resources/release");

    public static String getProjectName() {
        return RELEASE.getString("projname");
    }

    public static String getTabCaption() {
        return RELEASE.getString("tabcaption");
    }

    public static String getVersion() {
        return RELEASE.getString("version");
    }

    private static void usage() {
        System.out.println("");
        System.out.println(String.format("Usage: java -jar %s.jar [option] [-i, --interface <interface>] [--fakeip <fakeip>] [--fakeipv6 <fakeip>] [--fakedomains <FakeDomains>] [--nameservers <NameServers>] [-p, --port <dnPport>]", getProjectName()));
        System.out.println("[option]");
        System.out.println("\t-h, --help - help show");
        System.out.println("\t-v, --version - version show");
        System.out.println("\t-gui - GUI Mode ");
        System.out.println("\t--bind-list - list of bind interfaces ");
        System.out.println("[command]");
        System.out.println("\t-i, --interface <interface> - Specify the interface IP address.");
        System.out.println("\t--fakeip <fakeip> - Specify the IPv4 address to spoof");
        System.out.println("\t--fakeipv6 <fakeip> - Specify the IPv6 address to spoof");
        System.out.println("\t--fakedomains <FakeDomains> - Specify the domain to spoof");
        System.out.println("\t--nameservers <NameServers>  - Specify the name server");
        System.out.println("\t--p, -port <dnsPort> - Specify the DNS port");
        System.out.println("\t--disable-system-hosts - Disable DNS name resolution using the system hosts file");
        System.out.println("");
    }

    /*
        args = new String[]{"-gui"};
        args = new String[]{"-i","192.168.137.1", "--fakeip", "192.168.137.1", "--fakedomains", "www.example.com,www.example.jp"};
        args = new String[]{"-i","192.168.137.1", "--fakeip", "192.168.137.1", "--fakeipv6", "::1", "--fakedomains", "www.example.com,www.example.jp"};
     */
    public static void main(String[] args) {

        final FakeDnsProperty fakeDnsOption = new FakeDnsProperty();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                // --- 引数なしのオプション ---
                case "-v":
                case "--version": {
                    System.out.println("Version: " + getVersion());
                    System.out.println("Language: " + Locale.getDefault().getLanguage());
                    return;
                }
                case "-h":
                case "--help": {
                    usage();
                    return;
                }
                case "-gui": {
                    EventQueue.invokeLater(MainPanel::createAndShowGui);
                    return;
                }
                case "--disable-system-hosts": {
                    fakeDnsOption.setResolvSystemHosts(false);
                    break;
                }
                case "--bind-list": {
                    List<InetAddress> inet = NetUtil.getNetworkInterfaces();
                    for (InetAddress addr : inet) {
                        System.out.println(addr.getHostAddress());
                    }
                    return;
                }
                // --- 引数ありのオプション ---
                case "-i":
                case "--interface": {
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
                        String fakeIPv4 = args[++i];
                        fakeDnsOption.setFakeIPv4(fakeIPv4);
                    } else {
                        System.err.println(args[++i] + " requires arguments");
                        return;
                    }
                    break;
                }
                case "--fakeipv6": {
                    if (i + 1 < args.length) {
                        String fakeIPv6 = args[++i];
                        fakeDnsOption.setFakeIPv6(fakeIPv6);
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
        if (!(IpUtil.isIPv4Address(fakeDnsOption.getBindInterface()) || IpUtil.isIPv6Address(fakeDnsOption.getBindInterface()))) {
            System.out.println("-i IPv4 format error: " + fakeDnsOption.getBindInterface());
            usage();
            return;
        }

        // 偽装IP(ipv4)
        if (!fakeDnsOption.isEmptyFakeIPv4() && !IpUtil.isIPv4Address(fakeDnsOption.getFakeIPv4())) {
            System.out.println("--fakeip IPv4 format error: " + fakeDnsOption.getFakeIPv4());
            usage();
            return;
        }

        // 偽装IP(ipv6)
        if (!fakeDnsOption.isEmptyFakeIPv6() && !IpUtil.isIPv6Address(fakeDnsOption.getFakeIPv6())) {
            System.out.println("--fakeipv6 IPv6 format error: " + fakeDnsOption.getFakeIPv6());
            usage();
            return;
        }

        // fakeipv4 v6 は必須
        if (fakeDnsOption.isEmptyFakeIPv4() && fakeDnsOption.isEmptyFakeIPv6()) {
            System.out.println("fakeip has not been specified");
            usage();
            return;
        }

        System.out.println("Bind intarface: " + fakeDnsOption.getBindInterface());
        if (!fakeDnsOption.isEmptyFakeIPv4()) {
            System.out.println("FakeIPv4: " + fakeDnsOption.getFakeIPv4());
        }
        if (!fakeDnsOption.isEmptyFakeIPv6()) {
            System.out.println("FakeIPv6: " + fakeDnsOption.getFakeIPv6());
        }
        System.out.println("port:" + fakeDnsOption.getDnsPort());
        if (!fakeDnsOption.getFakeDomains().isEmpty()) {
            System.out.println("FakeDomains: " + HostNameItem.joinHostList(",", fakeDnsOption.getFakeDomains()));
        }
        if (!fakeDnsOption.getNameServers().isEmpty()) {
            System.out.println("NameServers: " + HostNameItem.joinHostList(",", fakeDnsOption.getNameServers()));
        }
        // CLI の場合は常に無効
        fakeDnsOption.setResolvBurpHosts(false);

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
                String exMessage = "";
                String exException = "";
                if (ex instanceof BindException) {
                    exMessage = "Bind Error: " + fakeDnsOption.getBindInterface() + " - " + ex.getMessage();
                } else if (ex instanceof SocketException) {
                    exMessage = "Socket Error: " + ex.getMessage();
                    exException = StringUtil.getStackTrace(ex);
                } else if (ex instanceof IOException) {
                    exMessage = "Fatal Error: " + ex.getMessage();
                    exException = StringUtil.getStackTrace(ex);
                }
                System.err.println(exMessage);
                if (!exException.isEmpty()) {
                    System.err.println(exException);
                }
            }
        });
        dnsServer.startServer();
        System.out.println("Main thread: DNS Server thread started.");
        dnsServer.joinServer();
        System.out.println("Main thread: terminate.");
    }

    private final burp.api.montoya.MontoyaApi api;
    private final FakeDnsProperty fakeDnsOption = new FakeDnsProperty();

    private DnsHandler dnsServer = null;

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
        FakeDnsProperty option = this.getFakeDnsOption();
        option.setBurpHosts(HostName.getInstance(burpHostList));
        // スレッドを作成して開始
        this.dnsServer = new DnsHandler(option);
        this.dnsServer.setEventHandler(this.getEventHandler());
        this.dnsServer.start();

    }

    public synchronized boolean isRunning() {
        return (this.dnsServer != null);
    }

    public synchronized void joinServer() {
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
            this.dnsServer.terminate();
            this.dnsServer.interrupt();
        }
        this.dnsServer = null;
    }

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

    private DnsHandler.MessageHandler messageHandler = null;

    public DnsHandler.MessageHandler getEventHandler() {
        return this.messageHandler;
    }

    public void setEventHandler(DnsHandler.MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    private final static class MainPanel extends JPanel {

        private final FakeDnsTab fakeDnsTab = new FakeDnsTab();

        private MainPanel() {
            super(new BorderLayout());
            Preferences pref = BurpPreferences.extensions(SimpleDnsServer.getProjectName());
            FakeDnsProperty option = new FakeDnsProperty();
            option.saveSetting(pref.getString(option.getSettingName()));
            this.fakeDnsTab.setProperty(option);
            this.fakeDnsTab.setStandalone(true);
            this.add(this.fakeDnsTab, BorderLayout.CENTER);
            setPreferredSize(new Dimension(800, 600));
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
                        Preferences pref = BurpPreferences.extensions(SimpleDnsServer.getProjectName());
                        pref.setString(option.getSettingName(), option.loadSetting());
                    }
                });
            } catch (UnsupportedLookAndFeelException ignored) {
                logger.log(Level.SEVERE, ignored.getMessage(), ignored);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                System.err.println(StringUtil.getStackTrace(ex.getMessage(), ex));
            }
        }
    }

}
