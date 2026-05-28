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

    public static final int IPv4_FAMILY = 4;
    public static final int IPv6_FAMILY = 6;

    public static final String ALL_IPv4 = "0.0.0.0";
    public static final String LOCAL_IPv4 = "127.0.0.1";
    public static final String LOCAL_IPv6 = "0:0:0:0:0:0:0:1";

    public static List<InetAddress> getNetworkInterfaces() {
        List<InetAddress> inet = new ArrayList<>();
        try {
            inet.add(Inet4Address.getByAddress(new byte[]{0, 0, 0, 0}));
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof java.net.Inet4Address inet4) {
                        inet.add(inet4); // IPv4
                    } else if (addr instanceof java.net.Inet6Address inet6) {
                        if (!inet6.isMulticastAddress() && !inet6.isLinkLocalAddress() && !inet6.isSiteLocalAddress()) {
                            inet.add(inet6); // IPv6
                        }
                    }
                }
            }
        } catch (SocketException | UnknownHostException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return inet;
    }

}
