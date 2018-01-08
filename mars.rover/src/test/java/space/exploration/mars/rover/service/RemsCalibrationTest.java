package space.exploration.mars.rover.service;

import junit.framework.TestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.kernel.SpacecraftClock;

import java.util.Properties;

public class RemsCalibrationTest extends TestCase {
    public static final String SEPARATOR = "-------------------------------------------------------------------";
    Logger             logger             = LoggerFactory.getLogger(SpacecraftClock.class);
    WeatherDataService remsWeatherStation = new WeatherDataService();

    @Override
    public void setUp() {
        System.out.println(SEPARATOR);
        System.out.println("Running REMS Calibration Test.");
        System.out.println(SEPARATOR);
    }

    @Test
    public void testClockInitialization() {
        remsWeatherStation.downloadCalibrationData(1210, 504878468.183908);
        System.out.println(remsWeatherStation.isCalibrated());
        System.out.println(SEPARATOR);
        System.out.println("End of REMS Calibration test!");
        System.out.println(SEPARATOR);
    }
}
//http://pds-atmospheres.nmsu.edu/PDS/data/mslrem_1001/DATA/SOL_01160_01293/SOL01210/RME_504863505RNV12100000000_______P1.TAB