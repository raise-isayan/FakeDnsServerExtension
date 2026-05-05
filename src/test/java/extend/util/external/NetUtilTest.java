package extend.util.external;

import java.net.InetAddress;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author isayan
 */
public class NetUtilTest {

    public NetUtilTest() {
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

    /**
     * Test of getNetworkInterfaces method, of class NetUtil.
     */
    @Test
    public void testGetNetworkInterfaces() {
        System.out.println("getNetworkInterfaces");
        List<InetAddress> expResult = null;
        List<InetAddress> result = NetUtil.getNetworkInterfaces();
        for (InetAddress r: result) {
            System.out.println("ip:" + r.getHostAddress());
        }
    }

}
