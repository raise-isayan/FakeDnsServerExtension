package fakedns.model;

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
public class FakeDnsItemTest {

    public FakeDnsItemTest() {
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
            String multiLine = "www.examle.jp";
            List<HostNameItem> expResult = List.of(new HostNameItem(true, "www.examle.jp"));
            List<HostNameItem> result = HostNameItem.parseHostList(multiLine);
            assertEquals(1, result.size());
            for (int i = 0; i < result.size(); i++) {
                assertEquals(expResult.get(i), result.get(i));
            }
        }
        {
            String multiLine = "www.examle.jp,www.examle.jp";
            List<HostNameItem> expResult = List.of(new HostNameItem(true, "www.examle.jp"), new HostNameItem(true, "www.examle.jp"));
            List<HostNameItem> result = HostNameItem.parseHostList(multiLine);
            assertEquals(2, result.size());
            for (int i = 0; i < result.size(); i++) {
                assertEquals(expResult.get(i), result.get(i));
            }
        }
        {
            String multiLine = "www.examle.jp\nwww.examle.jp\n";
            List<HostNameItem> expResult = List.of(new HostNameItem(true, "www.examle.jp"), new HostNameItem(true, "www.examle.jp"));
            List<HostNameItem> result = HostNameItem.parseHostList(multiLine);
            assertEquals(2, result.size());
            for (int i = 0; i < result.size(); i++) {
                assertEquals(expResult.get(i), result.get(i));
            }
        }
        {
            String multiLine = "www.examle.jp\r\nwww.examle.jp\r\n";
            List<HostNameItem> expResult = List.of(new HostNameItem(true, "www.examle.jp"), new HostNameItem(true, "www.examle.jp"));
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
            List<HostNameItem> domainList = List.of(new HostNameItem(true, "www.examle.com"), new HostNameItem(true, "www.examle.jp"));
            String expResult = "www.examle.com,www.examle.jp";
            String result = HostNameItem.joinHostList(",", domainList);
            assertEquals(expResult, result);
        }
        {
            List<HostNameItem> domainList = List.of(new HostNameItem(true, "www.examle.com"), new HostNameItem(false, "www.examle.co.jp"), new HostNameItem(true, "www.examle.jp"));
            String expResult = "www.examle.com,www.examle.jp";
            String result = HostNameItem.joinHostList(",", domainList);
            assertEquals(expResult, result);
        }
    }

}
