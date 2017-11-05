package space.exploration.mars.rover.kernel;

import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;
import space.exploration.mars.rover.bootstrap.MarsMissionLaunch;
import space.exploration.mars.rover.service.WeatherQueryOuterClass;

/**
 * This test is ignored for convenience building, if you change the weatherService - run this test to ensure fail
 * recovery.
 */
@Ignore
public class WeatherSensingStateTest extends TestCase {
    private Rover rover = null;

    @Override
    public void setUp() {
        MarsMissionLaunch.configureLogging(false);
        rover = SetupTestRover.setupTestRover();
    }

    @Test
    public void testInvalidEndDate() {
        System.out.println("Set up end date earlier than start date.");
        WeatherQueryOuterClass.WeatherQuery.Builder wBuilder = WeatherQueryOuterClass.WeatherQuery.newBuilder();
        wBuilder.setTerrestrialStartDate("2017-10-01");
        wBuilder.setTerrestrialEndDate("2017-09-31");

        rover.setState(rover.getWeatherSensingState());
        rover.senseWeather(wBuilder.build());

        System.out.println("Verify that the rover state is still listening state. Check logs for exception handled.");
        assertEquals(rover.getState(), rover.getListeningState());
    }
}
