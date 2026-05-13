package fakedns.model;

import com.google.gson.annotations.Expose;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author isayan
 */
public class HostNameItem {

    public HostNameItem() {

    }

    public HostNameItem(boolean enable, String hostName) {
        this.enable = enable;
        this.hostName = hostName;
    }

    @Expose
    private boolean enable;

    /**
     * @return the enable
     */
    public boolean isEnable() {
        return this.enable;
    }

    /**
     * @param enable the enable to set
     */
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Expose
    private String hostName;

    /**
     * @return the hostName
     */
    public String getHostName() {
        return this.hostName;
    }

    /**
     * @param hostName the hostName to set
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HostNameItem item) {
            return this.enable == item.enable && this.hostName.equalsIgnoreCase(item.hostName);
        }
        return false;
    }

    public void setProperty(HostNameItem item) {
        this.setEnable(item.isEnable());
        this.setHostName(item.getHostName());
    }

    public static Object[] toObjects(HostNameItem item) {
        Object[] beans = new Object[2];
        beans[0] = item.isEnable();
        beans[1] = item.getHostName();
        return beans;
    }

    public static HostNameItem fromObjects(Object[] rows) {
        HostNameItem item = new HostNameItem();
        item.setEnable(((Boolean) rows[0]));
        item.setHostName((String) rows[1]);
        return item;
    }

    public static List<HostNameItem> parseHostList(String multiLine) {
        List<HostNameItem> fakeList = new ArrayList<>();
        String hostNames[] = multiLine.split("(\\r?\\n)|(,)");
        for (String host : hostNames) {
            if (!host.isEmpty()) {
                fakeList.add(new HostNameItem(true, host));
            }
        }
        return fakeList;
    }

    public static String joinHostList(CharSequence delimiter, List<HostNameItem> hostNames) {
        return String.join(delimiter, toHostArray(hostNames));
    }

    public static String[] toHostArray(List<HostNameItem> hostNames) {
        return toHostArray(hostNames, false);
    }

    public static String[] toHostArray(List<HostNameItem> hostNames, boolean entryAll) {
        List<String> hosts = new ArrayList<>();
        for (HostNameItem host : hostNames) {
            if (host.isEnable() || entryAll) {
                hosts.add(host.getHostName());
            }
        }
        return hosts.toArray(String[]::new);
    }

}
