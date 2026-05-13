package fakedns.model;

import com.google.gson.annotations.Expose;
import extend.util.external.NetUtil;
import extension.burp.HostName;
import extension.burp.HostNameEntry;
import extension.burp.IPropertyConfig;
import extension.helpers.json.JsonUtil;
import fakedns.server.FakeDnsOption;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author isayan
 */
public class FakeDnsProperty implements FakeDnsOption, IPropertyConfig {

    public final static String FAKEDNS_PROPERTY = "FakeDns";

    @Expose
    private String bindInterface = NetUtil.ALL_IPv4;

    public void setBindInterface(String bindInterface) {
        this.bindInterface = bindInterface;
    }

    @Override
    public String getBindInterface() {
        return this.bindInterface;
    }

    @Expose
    private String fakeIPv4 = "";

    public void setFakeIPv4(String fakeIPv4) {
        this.fakeIPv4 = fakeIPv4;
    }

    @Override
    public String getFakeIPv4() {
        return this.fakeIPv4;
    }

    public boolean isEmptyFakeIPv4() {
        return this.fakeIPv4 != null && this.fakeIPv4.isEmpty();
    }

    @Expose
    private String fakeIPv6 = "";

    public void setFakeIPv6(String fakeIPv6) {
        this.fakeIPv6 = fakeIPv6;
    }

    @Override
    public String getFakeIPv6() {
        return this.fakeIPv6;
    }

    public boolean isEmptyFakeIPv6() {
        return this.fakeIPv6 != null && this.fakeIPv6.isEmpty();
    }

    @Expose
    private final List<HostNameItem> fakeDomains = new ArrayList<>();

    @Override
    public List<HostNameItem> getFakeDomains() {
        return this.fakeDomains;
    }

    public void setFakeDomains(List<HostNameItem> fakeDomains) {
        this.fakeDomains.clear();
        this.fakeDomains.addAll(fakeDomains);
    }

    @Expose
    private final List<HostNameItem> nameServers = new ArrayList<>();

    @Override
    public List<HostNameItem> getNameServers() {
        return this.nameServers;
    }

    public void setNameServers(List<HostNameItem> nameServers) {
        this.nameServers.clear();
        this.nameServers.addAll(nameServers);
    }

    @Expose
    private int dnsPort = 53;

    @Override
    public int getDnsPort() {
        return this.dnsPort;
    }

    public void setDnsPort(int dnsPort) {
        this.dnsPort = dnsPort;
    }

    private int dnsTTL = 15;

    @Override
    public int getDnsTTL() {
        return this.dnsTTL;
    }

    @Override
    public Optional<InetAddress> getBurpAddressForHost(String hostnname, int family) {
        if (this.burpHosts != null) {
            List<HostNameEntry> entrys = this.burpHosts.resolvHostNames(hostnname);
            try {
                for (HostNameEntry entry : entrys) {
                    InetAddress addr = entry.asHostInetAddress();
                    if (family == IPv4_FAMILY && addr instanceof Inet4Address) {
                        return Optional.of(addr);
                    }
                    if (family == IPv6_FAMILY && addr instanceof Inet6Address) {
                        return Optional.of(addr);
                    }
                }
            } catch (UnknownHostException ex) {
                // nothing
            }
        }
        return Optional.empty();
    }

    private HostName burpHosts = null;

    public void setBurpHosts(HostName burpHosts) {
        this.burpHosts = burpHosts;
    }

    public HostName getBurpHosts() {
        return this.burpHosts;
    }

    @Expose
    private boolean resolvSystemHosts = true;

    public void setResolvSystemHosts(boolean resolvSystemHosts) {
        this.resolvSystemHosts = resolvSystemHosts;
    }

    @Override
    public boolean isResolvSystemHosts() {
        return this.resolvSystemHosts;
    }

    @Expose
    private boolean resolvBurpHosts = true;

    public void setResolvBurpHosts(boolean resolvBurpHosts) {
        this.resolvBurpHosts = resolvBurpHosts;
    }

    @Override
    public boolean isResolvBurpHosts() {
        return this.resolvBurpHosts;
    }

    public void setProperty(FakeDnsProperty property) {
        this.bindInterface = property.bindInterface;
        this.fakeIPv4 = property.fakeIPv4;
        this.fakeIPv6 = property.fakeIPv6;
        this.setFakeDomains(property.fakeDomains);;
        this.setNameServers(property.nameServers);;
        this.dnsPort = property.dnsPort;
        this.dnsTTL = property.dnsTTL;
        this.resolvBurpHosts = property.resolvBurpHosts;
        this.resolvSystemHosts = property.resolvSystemHosts;
    }

    @Override
    public String getSettingName() {
        return FAKEDNS_PROPERTY;
    }

    @Override
    public void saveSetting(String value) {
        FakeDnsProperty property = JsonUtil.jsonFromString(value, FakeDnsProperty.class, true);
        this.setProperty(property);
    }

    @Override
    public String loadSetting() {
        return JsonUtil.jsonToString(this, true);
    }

    @Override
    public String defaultSetting() {
        FakeDnsProperty property = new FakeDnsProperty();
        return JsonUtil.jsonToString(property, true);
    }

}
