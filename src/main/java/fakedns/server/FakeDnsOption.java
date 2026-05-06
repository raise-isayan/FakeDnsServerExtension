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

    public Optional<InetAddress> getBurpAddressForHost(String hostnname);

    public String getBindInterface();

    public int getDnsPort();

    public String getFakeIP();

    public List<HostNameItem> getFakeDomains();

    public List<HostNameItem> getNameServers();

    public int getDnsTTL();

    public boolean isResolvSystemHosts();

    public boolean isResolvBurpHosts();


}
