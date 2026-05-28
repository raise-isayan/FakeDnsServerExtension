package fakedns.model;

import extend.util.external.NetUtil;
import extension.burp.HostName;
import fakedns.HostsFileParserTest;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author isayan
 */
public class FakeDnsPropertyTest {

    public FakeDnsPropertyTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testGetBurpAddressForHostIPv4() {
        System.out.println("testGetBurpAddressForHostIPv4");
        try {
            {
                URI test_host_uri = FakeDnsPropertyTest.class.getResource("/resources/hosts_ipv4").toURI();
                FakeDnsProperty prop = new FakeDnsProperty();
                HostName burpHosts = HostName.parseHosts(HostName.parseLines(Path.of(test_host_uri)));
                prop.setBurpHosts(burpHosts);
                Optional<InetAddress> entry = prop.getBurpAddressForHost("www.example.com", NetUtil.IPv4_FAMILY);
                assertTrue(entry.isPresent());
                if (entry.isPresent()) {
                    System.out.println("addr:" + entry.get().getHostAddress());
                }
                Optional<InetAddress> notfound = prop.getBurpAddressForHost("www.example.com", NetUtil.IPv6_FAMILY);
                assertTrue(notfound.isEmpty());
            }
        } catch (URISyntaxException ex) {
            fail(ex);
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    public void testGetBurpAddressForHostIPv6() {
        System.out.println("testGetBurpAddressForHostIPv6");
        try {
            {
                URI test_host_uri = FakeDnsPropertyTest.class.getResource("/resources/hosts_mix").toURI();
                FakeDnsProperty prop = new FakeDnsProperty();
                HostName burpHosts = HostName.parseHosts(HostName.parseLines(Path.of(test_host_uri)));
                prop.setBurpHosts(burpHosts);
                Optional<InetAddress> entryv4 = prop.getBurpAddressForHost("www.example.jp", NetUtil.IPv4_FAMILY);
                assertTrue(entryv4.isPresent());
                if (entryv4.isPresent()) {
                    System.out.println("addrv4:" + entryv4.get().getHostAddress());
                    assertEquals("198.51.100.1", entryv4.get().getHostAddress());
                }
                Optional<InetAddress> entryv6 = prop.getBurpAddressForHost("www.example.jp", NetUtil.IPv6_FAMILY);
                assertTrue(entryv6.isPresent());
                if (entryv6.isPresent()) {
                    assertEquals("2001:db8:3333:4444:5555:6666:7777:8888", entryv6.get().getHostAddress());
                    System.out.println("addrv6:" + entryv6.get().getHostAddress());
                }
            }
        } catch (URISyntaxException ex) {
            fail(ex);
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    public void testAsInetAddress() {
        System.out.println("testAsInetAddress");
        try {
            FakeDnsProperty prop = new FakeDnsProperty();
            {
                prop.setFakeIPv4("192.0.2.11");
                prop.setFakeIPv6("2001:db8:85a3::8a2e:370:7334");
                Inet4Address inet4 = prop.asFakeIPv4Address();
                assertEquals("192.0.2.11", inet4.getHostAddress());
                Inet6Address inet6 = prop.asFakeIPv6Address();
                assertEquals("2001:db8:85a3:0:0:8a2e:370:7334", inet6.getHostAddress());
            }
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
        try {
            FakeDnsProperty prop = new FakeDnsProperty();
            {
                prop.setFakeIPv4("2001:db8:85a3::8a2e:370:7334");
                Inet4Address inet4 = prop.asFakeIPv4Address();
                fail();
            }
        } catch (UnknownHostException ex) {
            //
        }
        try {
            FakeDnsProperty prop = new FakeDnsProperty();
            {
                prop.setFakeIPv6("192.0.2.11");
                Inet6Address inet6 = prop.asFakeIPv6Address();
                fail();
            }
        } catch (UnknownHostException ex) {
            //
        }
    }

    @Test
    public void testSetProperty() {
        System.out.println("testSetProperty");
        try {
            URI test_host_uri = HostsFileParserTest.class.getResource("/resources/hosts_ipv4").toURI();
            FakeDnsProperty instance = new FakeDnsProperty();
            instance.setDnsPort(453);
            assertEquals(453, instance.getDnsPort());
            instance.setDnsTTL(50);
            assertEquals(50, instance.getDnsTTL());
            instance.setResolvBurpHosts(false);
            assertFalse(instance.isResolvBurpHosts());
            instance.setResolvSystemHosts(false);
            assertFalse(instance.isResolvSystemHosts());
            instance.setFakeIPv4("192.168.2.11");
            assertEquals("192.168.2.11", instance.getFakeIPv4());
            instance.setFakeIPv6("2001:db8:85a3::8a2e:370:7334");
            assertEquals("2001:db8:85a3::8a2e:370:7334", instance.getFakeIPv6());
            HostName burpHosts = HostName.parseHosts(HostName.parseLines(Path.of(test_host_uri)));
            instance.setBurpHosts(burpHosts);

            FakeDnsProperty prop = new FakeDnsProperty();
            prop.setProperty(instance);
            assertEquals(prop.getDnsPort(), instance.getDnsPort());
            assertEquals(prop.getDnsTTL(), instance.getDnsTTL());
            assertEquals(prop.isResolvBurpHosts(), instance.isResolvBurpHosts());
            assertEquals(prop.isResolvSystemHosts(), instance.isResolvSystemHosts());
            assertEquals(prop.getFakeIPv4(), instance.getFakeIPv4());
            assertEquals(prop.getFakeIPv6(), instance.getFakeIPv6());
        } catch (IOException ex) {
            fail(ex);
        } catch (URISyntaxException ex) {
            fail(ex);
        }
    }

    /**
     * Test of defaultSetting method, of class FakeDnsProperty.
     */
    @Test
    public void testDefaultSetting() {
        System.out.println("defaultSetting");
        FakeDnsProperty instance = new FakeDnsProperty();
        String result = instance.defaultSetting();
        System.out.println("SettingName:" + instance.getSettingName());
        System.out.println("defaultSetting:" + result);
    }

}
