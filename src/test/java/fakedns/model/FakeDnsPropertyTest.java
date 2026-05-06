package fakedns.model;

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

    /**
     * Test of defaultSetting method, of class FakeDnsProperty.
     */
    @Test
    public void testDefaultSetting() {
        System.out.println("defaultSetting");
        FakeDnsProperty instance = new FakeDnsProperty();
//        String expResult = "";
        String result = instance.defaultSetting();
        System.out.println("SettingName:" + instance.getSettingName());
        System.out.println("defaultSetting:" + result);
//        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }

}
