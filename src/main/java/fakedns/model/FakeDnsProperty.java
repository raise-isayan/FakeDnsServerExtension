package fakedns.model;

import com.google.gson.annotations.Expose;
import extend.util.external.NetUtil;
import extension.burp.HostName;
import extension.burp.HostNameEntry;
import extension.burp.IPropertyConfig;
import extension.helpers.json.JsonUtil;
import fakedns.server.FakeDnsOption;
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
    private String bindInterface = NetUtil.ALL_IP;

    public void setBindInterface(String bindInterface) {
        this.bindInterface = bindInterface;
    }

    public String getBindInterface() {
        return this.bindInterface;
    }

    @Expose
    private String fakeIP = NetUtil.LOCAL_IPv4;

    public void setFakeIP(String fakeIP) {
        this.fakeIP = fakeIP;
    }

    public String getFakeIP() {
        return this.fakeIP;
    }

    @Expose
    private final List<HostNameItem> fakeDomains = new ArrayList<>();

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

    private int dnsPort = 53;

    @Override
    public int getDnsPort() {
        return this.dnsPort;
    }

    public void setDnsPort(int dnsPort) {
        this.dnsPort = dnsPort;
    }

    private int dnsTTL = 60;

    @Override
    public int getDnsTTL() {
        return this.dnsTTL;
    }

    @Override
    public Optional<InetAddress> getBurpAddressForHost(String hostnname) {
        if (this.burpHosts != null) {
            try {
                HostNameEntry entry = this.burpHosts.resolvHostName(hostnname);
                if (entry != null) {
                    return Optional.ofNullable(entry.asInetAddress());
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

    public void setProperty(FakeDnsProperty property) {
        this.bindInterface = property.bindInterface;
        this.fakeIP = property.fakeIP;
        this.setFakeDomains(property.fakeDomains);;
        this.setNameServers(property.nameServers);;
        this.dnsPort = property.dnsPort;
        this.dnsTTL = property.dnsTTL;
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
