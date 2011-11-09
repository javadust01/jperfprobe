package net.sf.jperfprobe;

import org.junit.*;
import static org.junit.Assert.*;

public class TimeFactoryTest {
    private String version;

    @Before
    public void setUp() {
        version = System.getProperty("java.version");

    }

    @After
    public void tearDown() {
        System.setProperty("java.version", version);
    }

    @Test
    public void testFactory() {
        System.setProperty("java.version", "1.6");
        Time t = TimeFactory.getTime();
        assertTrue("Wrong time implementastion", t instanceof SystemTimeByNanos);

        System.setProperty("java.version", "1.5");
        t = TimeFactory.getTime();
        assertTrue("Wrong time implementastion", t instanceof SystemTimeByNanos);

        System.setProperty("java.version", "1.4");
        t = TimeFactory.getTime();
        assertTrue("Wrong time implementastion", t instanceof SystemTimeByMillis);
    }
}
