package fakedns.server;

import extension.burp.HostName;
import extension.helpers.IpUtil;
import fakedns.model.FakeDnsProperty;
import fakedns.model.HostNameItem;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;
import org.xbill.DNS.hosts.HostsFileParser;
import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.AsynchronousCloseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author isayan
 */
public class DnsHandler extends Thread {

    private final static Logger logger = Logger.getLogger(DnsHandler.class.getName());

    private final static java.util.ResourceBundle RELEASE = java.util.ResourceBundle.getBundle("burp/resources/release");

    private static final int UDP_SIZE = 512;

    public static enum DnsResolv {
        FAKE_DOMAIN, SYSTEM_HOSTS, BURP_HOSTS
    };

    private final FakeDnsProperty option;

    // システムデフォルトのDNS設定を使用するリゾルバ
    private Resolver systemResolver;
    private final HostsFileParser hostsParser;

    public DnsHandler(FakeDnsProperty option) {
        this.option = option;
        // ExtendedResolverを引数なしで生成すると、OSのデフォルトDNS設定を読み込む
        if (this.option.getNameServers().isEmpty()) {
            this.systemResolver = new ExtendedResolver();
        } else {
            try {
                this.systemResolver = new ExtendedResolver(HostNameItem.toHostArray(this.option.getNameServers()));
            } catch (UnknownHostException ex) {
                this.fireEventMessage(Thread.currentThread(), ex);
            }
        }
        File hostsFile = HostName.getSystemHostFile();
        this.hostsParser = new HostsFileParser(hostsFile.toPath());
        this.socket = null;
    }

    private String getLogName() {
        return "[" + FakeDnsProperty.FAKEDNS_PROPERTY + "] ";
    }

    protected boolean isFakeDomain(Name queryName) {
        final List<HostNameItem> fakeDomains = this.option.getFakeDomains();
        return fakeDomains.stream().anyMatch(predicate -> predicate.isEnable() && predicate.getHostName().equalsIgnoreCase(queryName.toString(true)));
    }

    private DatagramSocket socket = null;

    @Override
    public void run() {
        InetSocketAddress bindAddress = new InetSocketAddress(this.option.getBindInterface(), option.getDnsPort());
        try {
            this.socket = new DatagramSocket(null);
            this.socket.setReuseAddress(true);
            this.socket.bind(bindAddress);
//            socket.setSoTimeout(1000);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    terminate();
                }
            }));

            this.fireEventMessage("Accept DNS: " + bindAddress.getHostString() + ":" + bindAddress.getPort());
            byte[] buffer = new byte[UDP_SIZE];

            while (!Thread.currentThread().isInterrupted()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                this.socket.receive(packet);
                Message query = new Message(packet.getData());
                Record question = query.getQuestion();
                if (question == null) {
                    continue;
                }
                Name queryName = question.getName();
                int queryType = question.getType();
                Message response = null;

                // --- 判定ロジック ---
                if (this.isFakeDomain(queryName)) {
                    // 偽装処理
                    if (queryType == Type.A && !this.option.isEmptyFakeIPv4() && IpUtil.isIPv4Address(this.option.getFakeIPv4())) {
                        response = this.createResponse(query, question, this.option.asFakeIPv4Address(), DnsResolv.FAKE_DOMAIN);
//                        this.fireEventMessage("FakeIPv4: " + Type.string(queryType) + " - " + queryName.toString(true) + " [" + this.option.getFakeIPv4() + "]");
                    }
                    if (queryType == Type.AAAA && !this.option.isEmptyFakeIPv6() && IpUtil.isIPv6Address(this.option.getFakeIPv6())) {
                        response = this.createResponse(query, question, this.option.asFakeIPv6Address(), DnsResolv.FAKE_DOMAIN);
//                        this.fireEventMessage("FakeIPv6: " + Type.string(queryType) + " - " + queryName.toString(true) + " [" + this.option.getFakeIPv6() + "]");
                    }
                } else {
                    // Burp のHost
                    if (response == null && this.option.isResolvBurpHosts()) {
                        int family = (queryType == Type.AAAA) ? FakeDnsProperty.IPv6_FAMILY : FakeDnsProperty.IPv4_FAMILY;
                        Optional<InetAddress> burpHostIP = this.option.getBurpAddressForHost(queryName.toString(true), family);
                        if (burpHostIP.isPresent()) {
                            response = this.createResponse(query, question, burpHostIP.get(), DnsResolv.BURP_HOSTS);
                        }
                    }
                    if (response == null && this.option.isResolvSystemHosts()) {
                        // OS の hosts ファイルを確認
                        int family = (queryType == Type.AAAA) ? Address.IPv6 : Address.IPv4;
                        Optional<InetAddress> systemHostIP = this.hostsParser.getAddressForHost(queryName, family);
                        if (systemHostIP.isPresent()) {
                            response = this.createResponse(query, question, systemHostIP.get(), DnsResolv.SYSTEM_HOSTS);
                        }
                    }
                }
                if (response == null) {
                    // 転送（プロキシ）処理
                    this.fireEventMessage("resolv nameserver: " + Type.string(queryType) + " - " + queryName.toString(true));
                    response = this.forwardQuery(query);
                }
                // レスポンス送信
                if (response != null) {
                    byte[] respData = response.toWire();
                    DatagramPacket respPacket = new DatagramPacket(respData, respData.length, packet.getAddress(), packet.getPort());
                    this.socket.send(respPacket);
                }
            }
        } catch (BindException ex) {
            this.fireEventMessage(Thread.currentThread(), ex);
        } catch (SocketException ex) {
            // close の場合はException扱いとしない
            if (ex.getCause() instanceof AsynchronousCloseException) {
                this.fireEventMessage(ex.getMessage());
            }
            else {
                this.fireEventMessage(Thread.currentThread(), ex);
            }
        } catch (IOException ex) {
            this.fireEventMessage(Thread.currentThread(), ex);
        } finally {
            if (this.socket != null && !this.socket.isClosed()) {
                this.socket.close();
            }
        }

    }

    public synchronized void terminate() {
        if (this.socket != null && !this.socket.isClosed()) {
            this.socket.close();
        }
        // Thread.currentThread().interrupt(); // synchronized 内では不可
    }

    // 偽装レスポンスの組み立て
    private Message createResponse(Message query, Record question, InetAddress addr, DnsResolv resolvType) {
        Message response = new Message(query.getHeader().getID());
        response.getHeader().setFlag(Flags.QR);
        response.getHeader().setFlag(Flags.AA);
        response.getHeader().setFlag(Flags.RA);
        if (query.getHeader().getFlag(Flags.RD)) {
            response.getHeader().setFlag(Flags.RD);
        }
        response.addRecord(question, Section.QUESTION);
        Name queryName = question.getName();
        int queryType = question.getType();
        int queryClass = question.getDClass();

        this.fireEventMessage("resolv <" + resolvType.name() + ">: " + Type.string(queryType) + " - " + question.getName().toString(true) + " [" + addr.getHostAddress() + "]");

        Record answer = null;
        switch (queryType) {
            case Type.A: // IPv4対応
            {
                if (addr instanceof Inet4Address ipv4Addr) {
                    answer = new ARecord(queryName, DClass.IN, question.getTTL(), ipv4Addr);
                }
                break;
            }
            case Type.AAAA: // IPv6対応
            {
                if (addr instanceof Inet6Address ipv6Addr) {
                    answer = new AAAARecord(queryName, DClass.IN, question.getTTL(), ipv6Addr);
                }
                break;
            }
            case Type.HTTPS: {
                try {
                    List<HTTPSRecord.ParameterBase> params = new ArrayList<>();

                    // ALPNの設定 (h1, h2)
                    HTTPSRecord.ParameterAlpn alpn = new HTTPSRecord.ParameterAlpn();
                    alpn.fromString("h1,h2");
                    params.add(alpn);

                    // ipv4hintの設定
                    if (addr instanceof Inet4Address ipv4Addr) {
                        HTTPSRecord.ParameterIpv4Hint ipv4hint = new HTTPSRecord.ParameterIpv4Hint(List.of(ipv4Addr));
                        params.add(ipv4hint);
                    }
                    if (addr instanceof Inet6Address ipv6Addr) {
                        HTTPSRecord.ParameterIpv6Hint ipv6hint = new HTTPSRecord.ParameterIpv6Hint(List.of(ipv6Addr));
                        params.add(ipv6hint);
                    }
                    answer = new HTTPSRecord(queryName, queryClass, question.getTTL(), 0, queryName, params);

                } catch (TextParseException ex) {
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                }
                break;
            }
            default: {
                // 未対応のタイプはnull
                this.fireEventMessage("Unknown Type: " + Type.string(queryType));
                return null;
            }
        }

        if (answer != null) {
            response.addRecord(answer, Section.ANSWER);
        }
        return response;
    }

    // デフォルトDNSへの問い合わせ転送
    private Message forwardQuery(Message query) {
        try {
            // システムリゾルバへクエリをそのまま送信
            return this.systemResolver.send(query);
        } catch (IOException ex) {
            this.fireEventMessage(Thread.currentThread(), ex);
            System.err.println(this.getLogName() + "Forwarding failed: " + ex.getMessage());
            return null;
        }
    }

    public interface MessageHandler {

        public void message(String message);

        public void catchException(Thread t, Throwable e);

    }

    private MessageHandler messageHandler = null;

    public MessageHandler getEventHandler() {
        return this.messageHandler;
    }

    public void setEventHandler(MessageHandler exceptionHandler) {
        this.messageHandler = exceptionHandler;
    }

    public void fireEventMessage(String message) {
        if (this.messageHandler != null) {
            this.messageHandler.message(this.getLogName() + message);
        }
    }

    public void fireEventMessage(Thread t, Throwable e) {
        if (this.messageHandler != null) {
            this.messageHandler.catchException(t, e);
        }
    }
}
