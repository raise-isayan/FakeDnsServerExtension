package extend.util.external;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author isayan
 */
public class NetUtil {
    private final static Logger logger = Logger.getLogger(NetUtil.class.getName());

    public static final String ALL_IP = "0.0.0.0";
    public static final String LOCAL_IPv4 = "127.0.0.1";

    public static List<InetAddress> getNetworkInterfaces() {
        List<InetAddress> inet = new ArrayList<>();
        try {
            inet.add(Inet4Address.getByAddress(new byte[]{0, 0, 0, 0}));
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();

                // 有効かつループバックでないものを選択
//                if (!iface.isUp() || iface.isLoopback()) continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // IPv4のみ表示
                    if (addr instanceof java.net.Inet4Address) {
                        inet.add(addr);
                    }
                }
            }
        } catch (SocketException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        } catch (UnknownHostException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return inet;
    }
}
