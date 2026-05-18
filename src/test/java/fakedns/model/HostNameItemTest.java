package fakedns.model;

import java.util.ArrayList;
import java.util.List;
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
public class HostNameItemTest {

    public HostNameItemTest() {
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
    public void testParseHostList() {
        System.out.println("parseHostList");
        {
            String multiLine = "";
            List<HostNameItem> result = HostNameItem.parseHostList(multiLine);
            assertEquals(0, result.size());
        }
        {
            String multiLine = "www.example.jp";
            List<HostNameItem> expResult = List.of(new HostNameItem(true, "www.example.jp"));
            List<HostNameItem> result = HostNameItem.parseHostList(multiLine);
            assertEquals(1, result.size());
            for (int i = 0; i < result.size(); i++) {
                assertEquals(expResult.get(i), result.get(i));
            }
        }
        {
            String multiLine = "www.example.jp,www.example.jp";
            List<HostNameItem> expResult = List.of(new HostNameItem(true, "www.example.jp"), new HostNameItem(true, "www.example.jp"));
            List<HostNameItem> result = HostNameItem.parseHostList(multiLine);
            assertEquals(2, result.size());
            for (int i = 0; i < result.size(); i++) {
                assertEquals(expResult.get(i), result.get(i));
            }
        }
        {
            String multiLine = "www.example.jp\nwww.example.jp\n";
            List<HostNameItem> expResult = List.of(new HostNameItem(true, "www.example.jp"), new HostNameItem(true, "www.example.jp"));
            List<HostNameItem> result = HostNameItem.parseHostList(multiLine);
            assertEquals(2, result.size());
            for (int i = 0; i < result.size(); i++) {
                assertEquals(expResult.get(i), result.get(i));
            }
        }
        {
            String multiLine = "www.example.jp\r\nwww.example.jp\r\n";
            List<HostNameItem> expResult = List.of(new HostNameItem(true, "www.example.jp"), new HostNameItem(true, "www.example.jp"));
            List<HostNameItem> result = HostNameItem.parseHostList(multiLine);
            assertEquals(2, result.size());
            for (int i = 0; i < result.size(); i++) {
                assertEquals(expResult.get(i), result.get(i));
            }
        }
    }

    @Test
    public void testJoinHostList() {
        System.out.println("testJoinHostList");
        {
            List<HostNameItem> domainList = List.of(new HostNameItem(true, "www.example.com"), new HostNameItem(true, "www.example.jp"));
            String expResult = "www.example.com,www.example.jp";
            String result = HostNameItem.joinHostList(",", domainList);
            assertEquals(expResult, result);
        }
        {
            List<HostNameItem> domainList = List.of(new HostNameItem(true, "www.example.com"), new HostNameItem(false, "www.example.co.jp"), new HostNameItem(true, "www.example.jp"));
            String expResult = "www.example.com,www.example.jp";
            String result = HostNameItem.joinHostList(",", domainList);
            assertEquals(expResult, result);
        }
    }

    @Test
    public void testParseHostEquals() {
        System.out.println("testParseHostEquals");
        {
            HostNameItem expResult = new HostNameItem(true, "www.example.com");
            HostNameItem result = new HostNameItem(true, "WWW.EXAMPLE.COM");
            assertTrue(expResult.equals(result));
        }
        {
            HostNameItem expResult = new HostNameItem(true, "www.example.com");
            HostNameItem result = new HostNameItem(false, "WWW.EXAMPLE.COM");
            assertFalse(expResult.equals(result));
        }
    }

    @Test
    public void testHostNameItem() {
        System.out.println("testHostNameItem");
        HostNameItem instance = new HostNameItem(true, "www.example.com");
        {
            boolean result = instance.isEnable();
            assertEquals(true, result);
        }
        {
            String result = instance.getHostName();
            assertEquals("www.example.com", result);
        }
        {
            instance.setEnable(false);
            instance.setHostName("www.example.jp");
            assertFalse(instance.isEnable());
            String result = instance.getHostName();
            assertEquals("www.example.jp", result);
        }
    }

    @Test
    public void testHostArray() {
        List<HostNameItem> hostNames = new ArrayList<>();
        hostNames.add(new HostNameItem(true, "www.example.com"));
        hostNames.add(new HostNameItem(false, "www.example.co.jp"));
        hostNames.add(new HostNameItem(true, "www.example.jp"));
        String[] hosts = HostNameItem.toHostArray(hostNames);
        assertEquals(2, hosts.length);
        String[] hostsAll = HostNameItem.toHostArray(hostNames, true);
        assertEquals(3, hostsAll.length);
    }

}
