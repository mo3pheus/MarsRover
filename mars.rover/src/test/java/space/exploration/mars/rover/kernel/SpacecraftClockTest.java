package space.exploration.mars.rover.kernel;

import junit.framework.TestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.bootstrap.MarsMissionLaunch;
import space.exploration.mars.rover.bootstrap.MatrixCreation;

import java.io.IOException;
import java.util.Properties;

public class SpacecraftClockTest extends TestCase {

    public static final String SEPARATOR = "-------------------------------------------------------------------";
    Properties      marsConfig = null;
    Logger          logger     = LoggerFactory.getLogger(SpacecraftClock.class);
    SpacecraftClock clock      = null;

    @Override
    public void setUp() {
        MarsMissionLaunch.configureLogging(false);
        try {
            marsConfig = MatrixCreation.getConfig();
        } catch (IOException io) {
            logger.error("Can't open properties file marsConfig.properties", io);
        }

        clock = new SpacecraftClock(marsConfig);

        System.out.println(SEPARATOR);
        System.out.println("Running Spacecraft clock test.");
        System.out.println(SEPARATOR);
        System.out.println("Clock Equipment Name = " + clock.getEquipmentName());
        System.out.println("Clock Lifespan in millis = " + clock.getLifeSpan());
    }

    @Test
    public void testClockInitialization() {
        clock.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 10; i++) {
            System.out.println("Clock internal time = " + clock.getInternalClock() +
                                       " displayInternalClock = " + clock.displayInternalClock() +
                                       " corresponding sclk is = " +
                                       clock.getSclkTime());
        }
        System.out.println(clock.toString());
        clock.stopClock();

        System.out.println(SEPARATOR);
        System.out.println("End of Spacecraft clock test!");
        System.out.println(SEPARATOR);
    }
}
