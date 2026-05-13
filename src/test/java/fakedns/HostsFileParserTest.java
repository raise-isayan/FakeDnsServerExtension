package fakedns;

import extension.burp.HostName;
import extension.burp.HostNameEntry;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;
import org.xbill.DNS.hosts.HostsFileParser;

/**
 *
 * @author isayan
 */
public class HostsFileParserTest {

    public HostsFileParserTest() {
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
    public void testHostsFileParserIPv4() {
        System.out.println("testHostsFileParserIPv4");
        try {
            URI test_host_uri = HostsFileParserTest.class.getResource("/resources/hosts_ipv4").toURI();
            {
                HostsFileParser hostsParser = new HostsFileParser(Path.of(test_host_uri));
                Optional<InetAddress> host = hostsParser.getAddressForHost(Name.fromString("www.example.com."), Type.A);
                assertTrue(host.isPresent());
                assertTrue(host.get() instanceof Inet4Address);
                assertEquals("192.168.0.1", host.get().getHostAddress());
                System.out.println(host.get());
            }
            {
                HostsFileParser hostsParser = new HostsFileParser(Path.of(test_host_uri));
                Optional<InetAddress> host = hostsParser.getAddressForHost(Name.fromString("www.example.jp."), Type.AAAA);
                assertTrue(host.isEmpty());
            }
        } catch (URISyntaxException ex) {
            fail(ex);
        } catch (TextParseException ex) {
            fail(ex);
        } catch (IOException ex) {
            fail(ex);
        }

    }

    @Test
    public void testHostsFileParserMix() {
        System.out.println("testHostsFileParserMix");
        try {
            URI test_host_uri = HostsFileParserTest.class.getResource("/resources/hosts_mix").toURI();
            {
                HostsFileParser hostsParser = new HostsFileParser(Path.of(test_host_uri));
                Optional<InetAddress> host = hostsParser.getAddressForHost(Name.fromString("www.example.com."), Type.A);
                assertTrue(host.isPresent());
                assertTrue(host.get() instanceof Inet4Address);
                assertEquals("192.0.2.1", host.get().getHostAddress());
                System.out.println(host.get());
            }
            {
                HostsFileParser hostsParser = new HostsFileParser(Path.of(test_host_uri));
                Optional<InetAddress> host = hostsParser.getAddressForHost(Name.fromString("www.example.net."), Type.A);
                assertTrue(host.isPresent());
                assertTrue(host.get() instanceof Inet4Address);
                assertEquals("203.0.113.1", host.get().getHostAddress());
                System.out.println(host.get());
            }
            {
                HostsFileParser hostsParser = new HostsFileParser(Path.of(test_host_uri));
                Optional<InetAddress> host = hostsParser.getAddressForHost(Name.fromString("www.example.org."), Type.A);
                assertTrue(host.isPresent());
                assertTrue(host.get() instanceof Inet4Address);
                assertEquals("203.0.113.1", host.get().getHostAddress());
                System.out.println(host.get());
            }
            {
                HostsFileParser hostsParser = new HostsFileParser(Path.of(test_host_uri));
                Optional<InetAddress> host = hostsParser.getAddressForHost(Name.fromString("ww2.example.com."), Type.A);
                assertTrue(host.isEmpty());
            }
            {
                HostsFileParser hostsParser = new HostsFileParser(Path.of(test_host_uri));
                Optional<InetAddress> host = hostsParser.getAddressForHost(Name.fromString("www.example.jp."), Type.AAAA);
                assertTrue(host.isPresent());
                assertTrue(host.get() instanceof Inet6Address);
                assertEquals("2001:db8:3333:4444:5555:6666:7777:8888", host.get().getHostAddress());
                System.out.println(host.get());
            }
            {
                HostsFileParser hostsParser = new HostsFileParser(Path.of(test_host_uri));
                Optional<InetAddress> host = hostsParser.getAddressForHost(Name.fromString("www.example.co.jp."), Type.AAAA);
                assertTrue(host.isPresent());
                assertTrue(host.get() instanceof Inet6Address);
                assertEquals("2001:db8:0:0:0:0:0:1", host.get().getHostAddress());
                System.out.println(host.get());
            }
        } catch (URISyntaxException ex) {
            fail(ex);
        } catch (TextParseException ex) {
            fail(ex);
        } catch (IOException ex) {
            fail(ex);
        }
    }

    @Test
    public void testHostsFile() {
        try {
            System.out.println("testHostsFile");
            URI test_host_uri = HostsFileParserTest.class.getResource("/resources/hosts_ipv4").toURI();
            HostsFileParser hostsParser = new HostsFileParser(Path.of(test_host_uri));
            Optional<InetAddress> host = hostsParser.getAddressForHost(Name.fromString("www.example.com."), Type.A);
            HostName hostName = HostName.parseHosts(HostName.parseLines(Path.of(test_host_uri)));
            HostNameEntry entry = hostName.resolvHostName("www.example.com");
            assertTrue(host.isPresent());
            assertTrue(entry.getIPAddress().equals(host.get().getHostAddress()));
        } catch (URISyntaxException ex) {
            fail(ex);
        } catch (IOException ex) {
            fail(ex);
        }
    }

}
