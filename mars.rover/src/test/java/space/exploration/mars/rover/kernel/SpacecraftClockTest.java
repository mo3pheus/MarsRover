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
    }

    @Test
    public void testClockInitialization() {
        clock = new SpacecraftClock(marsConfig);
        clock.start();
        for (int i = 0; i < 10; i++) {
            System.out.println(clock.getSclkTime());
        }
        clock.stopClock();
    }
}
