package fakedns;

import extension.helpers.StringUtil;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class SimpleDnsSpoofer {

    private static final int DNS_PORT = 53;
    // 待ち受けたいインターフェースのIPアドレスを指定
    private static final String BIND_ADDRESS = "192.168.137.1"; // 例: Host-Only Networkなど
    private static final String TARGET_DOMAIN = "www.example.com."; // 最後にドットが必要
    private static final String SPOOFED_IP = "192.168.137.1";

    public static void main(String[] args) {
        try {
            // InetSocketAddress を使用して特定のインターフェースにバインド
            InetSocketAddress bindAddress = new InetSocketAddress(BIND_ADDRESS, DNS_PORT);
            DatagramSocket socket = new DatagramSocket(bindAddress);
            System.out.println("DNS Spoofing server started on port " + DNS_PORT);
            System.out.println("Spoofing: " + TARGET_DOMAIN + " -> " + SPOOFED_IP);

            byte[] buffer = new byte[512]; // DNS UDP パケットは通常512バイト以内

            while (true) {
                // 1. クエリの受信
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                try {
                    // 2. DNS メッセージとして解析
                    Message query = new Message(packet.getData());
                    Record question = query.getQuestion();
                    String queryName = question.getName().toString();

                    System.out.println("Query received for: " + queryName);

                    // 3. 偽装対象のドメインかチェック
                    if (queryName.equalsIgnoreCase(TARGET_DOMAIN)) {
                        // 応答メッセージの作成
                        Message response = new Message(query.getHeader().getID());
                        response.getHeader().setFlag(Flags.QR); // Response flag
                        response.getHeader().setFlag(Flags.AA); // Authoritative Answer flag
                        response.addRecord(question, Section.QUESTION);

                        // 偽の A レコードを作成 (TTLは60秒に設定)
                        Record answer = new ARecord(
                                Name.fromString(TARGET_DOMAIN),
                                DClass.IN,
                                60,
                                InetAddress.getByName(SPOOFED_IP)
                        );
                        response.addRecord(answer, Section.ANSWER);

                        // 4. パケットの送信
                        byte[] respData = response.toWire();
                        DatagramPacket respPacket = new DatagramPacket(
                                respData, respData.length, packet.getAddress(), packet.getPort()
                        );
                        socket.send(respPacket);
                        System.out.println("  -> Spoofed response sent!");
                    }
                } catch (Exception e) {
                    System.err.println(StringUtil.getStackTrace(e.getMessage(), e));
                }
            }
        } catch (IOException e) {
            System.err.println(StringUtil.getStackTrace(e.getMessage(), e));
        }
    }

//        pref.setBoolean("bbool", true);
//        pref.setByte("bbyte", (byte)127);
//        pref.setShort("sshort", (short)255);
//        pref.setInteger("int", 6555);
//        pref.setLong("llong", 123456789);
}
