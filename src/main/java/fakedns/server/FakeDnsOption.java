package fakedns.server;

import fakedns.model.HostNameItem;
import java.net.InetAddress;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author isayan
 */
public interface FakeDnsOption {

    public static final int IPv4_FAMILY = 4;
    public static final int IPv6_FAMILY = 6;

    public Optional<InetAddress> getBurpAddressForHost(String hostnname, int family);

    public String getBindInterface();

    public int getDnsPort();

    public String getFakeIPv4();

    public String getFakeIPv6();

    public List<HostNameItem> getFakeDomains();

    public List<HostNameItem> getNameServers();

    public int getDnsTTL();

    public boolean isResolvSystemHosts();

    public boolean isResolvBurpHosts();

}
