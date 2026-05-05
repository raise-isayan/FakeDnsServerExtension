package fakedns.server;

import extension.burp.HostName;
import fakedns.model.HostNameItem;
import java.io.File;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import org.xbill.DNS.hosts.HostsFileParser;

/**
 *
 * @author isayan
 */
public class DnsHandler implements Runnable {

    private final static Logger logger = Logger.getLogger(DnsHandler.class.getName());

    public static enum DnsResolv {
        FAKE_DOMAIN, SYSTEM_HOSTS, BURP_HOSTS
    };

    private final FakeDnsOption option;

    // システムデフォルトのDNS設定を使用するリゾルバ
    private Resolver systemResolver;
    private final HostsFileParser hostsParser;

    public DnsHandler(FakeDnsOption option) {
        this.option = option;
        // ExtendedResolverを引数なしで生成すると、OSのデフォルトDNS設定を読み込む
        if (this.option.getNameServers().isEmpty()) {
            this.systemResolver = new ExtendedResolver();
        } else {
            try {
                this.systemResolver = new ExtendedResolver(HostNameItem.toStringArray(this.option.getNameServers()));
            } catch (UnknownHostException ex) {
                if (this.messageHandler != null) {
                    this.messageHandler.catchException(Thread.currentThread(), ex);
                }
            }
        }
        File hostsFile = HostName.getSystemHostFile();
        this.hostsParser = new HostsFileParser(hostsFile.toPath());
    }

    public void run() {
        InetSocketAddress bindAddress = new InetSocketAddress(this.option.getBindInterface(), option.getDnsPort());
        try (DatagramSocket socket = new DatagramSocket(null)) {
            socket.setReuseAddress(true);
            socket.bind(bindAddress);
            if (this.messageHandler != null) {
                this.messageHandler.message("[DNS-Thread] Accept DNS: " + bindAddress.getHostString() + ":" + bindAddress.getPort());
            }
            byte[] buffer = new byte[512];

            while (!Thread.currentThread().isInterrupted()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                Message query = new Message(packet.getData());
                Record question = query.getQuestion();
                if (question == null) {
                    continue;
                }

                Name queryName = question.getName();
                Message response = null;

                // --- 判定ロジック ---
                List<HostNameItem> fakeDomains = this.option.getFakeDomains();
                if (fakeDomains.stream().anyMatch(predicate -> predicate.isEnable() && predicate.getHostName().equalsIgnoreCase(queryName.toString(true)))) {
                    // 偽装処理
                    response = createResponse(query, question, InetAddress.getByName(this.option.getFakeIP()), DnsResolv.FAKE_DOMAIN);
                    if (this.messageHandler != null) {
                        this.messageHandler.message("[DNS-Thread] FakeIP: " + queryName.toString(true) + "(" + this.option.getFakeIP() + ")");
                    }
                } else {
                    // Burp のHost
                    Optional<InetAddress> burpHostIP = this.option.getBurpAddressForHost(queryName.toString(true));
                    if (burpHostIP.isPresent()) {
                        if (this.messageHandler != null) {
                            this.messageHandler.message("[DNS-Thread] burp hosts: " + queryName.toString(true) + "(" + burpHostIP.get() + ")");
                        }
                        response = createResponse(query, question, burpHostIP.get(), DnsResolv.BURP_HOSTS);
                    } else {
                        // OS の hosts ファイルを確認
                        Optional<InetAddress> systemHostIP = hostsParser.getAddressForHost(queryName, Address.IPv4);
                        if (systemHostIP.isPresent()) {
                            if (this.messageHandler != null) {
                                this.messageHandler.message("[DNS-Thread] system hosts: " + queryName.toString(true) + "(" + systemHostIP.get() + ")");
                            }
                            response = createResponse(query, question, systemHostIP.get(), DnsResolv.SYSTEM_HOSTS);
                        } else {
                            // 転送（プロキシ）処理
                            if (this.messageHandler != null) {
                                this.messageHandler.message("[DNS-Thread] system:" + queryName.toString(true));
                            }
                            response = forwardQuery(query);
                        }
                    }
                }

                // レスポンス送信
                if (response != null) {
                    byte[] respData = response.toWire();
                    DatagramPacket respPacket = new DatagramPacket(
                        respData, respData.length, packet.getAddress(), packet.getPort()
                    );
                    socket.send(respPacket);
                }
            }
        } catch (IOException ex) {
            if (this.messageHandler != null) {
                this.messageHandler.catchException(Thread.currentThread(), ex);
            }
        }
    }

    // 偽装レスポンスの組み立て
    private Message createResponse(Message query, Record question, InetAddress addr, DnsResolv resolvType) {
        if (this.messageHandler != null) {
            this.messageHandler.message("[DNS-Thread] Resolved via " + resolvType.name() + ": " + question.getName() + " -> " + addr.getHostAddress());
        }
        Message response = new Message(query.getHeader().getID());
        response.getHeader().setFlag(Flags.QR);
        response.getHeader().setFlag(Flags.AA);
        response.addRecord(question, Section.QUESTION);
        Record answer = new ARecord(question.getName(), DClass.IN, this.option.getDnsTTL(), addr);
        response.addRecord(answer, Section.ANSWER);
        return response;
    }

    // デフォルトDNSへの問い合わせ転送
    private Message forwardQuery(Message query) {
        try {
            // システムリゾルバへクエリをそのまま送信
            return systemResolver.send(query);
        } catch (IOException ex) {
            if (this.messageHandler != null) {
                this.messageHandler.catchException(Thread.currentThread(), ex);
            }
            System.err.println("[DNS-Thread] Forwarding failed: " + ex.getMessage());
            return null;
        }
    }

    public interface MessageHandler {

        public void message(String message);

        public void catchException(Thread t, Throwable e);

    }

    private MessageHandler messageHandler = null;

    public MessageHandler getExceptionHandler() {
        return this.messageHandler;
    }

    public void setExceptionHandler(MessageHandler exceptionHandler) {
        this.messageHandler = exceptionHandler;
    }

}
