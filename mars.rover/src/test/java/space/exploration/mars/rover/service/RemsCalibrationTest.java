package space.exploration.mars.rover.service;

import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.kernel.SpacecraftClock;
import space.exploration.mars.rover.service.calibration.weather.WeatherDataService;

import java.util.Properties;

@Ignore
public class RemsCalibrationTest extends TestCase {
    public static final String SEPARATOR = "-------------------------------------------------------------------";
    Logger             logger             = LoggerFactory.getLogger(SpacecraftClock.class);
    WeatherDataService remsWeatherStation = new WeatherDataService();
    Properties         marsConfig         = null;
    SpacecraftClock    clock              = null;

    @Override
    public void setUp() {
        //http://pds-atmospheres.nmsu
        // .edu/PDS/data/mslrem_1001/DATA/SOL_01160_01293/SOL01210/RME_504863505RNV12100000000_______P1.TAB
        System.out.println(SEPARATOR);
        System.out.println("Running REMS Calibration Test.");
        System.out.println(SEPARATOR);
    }

    @Test
    public void testClockInitialization() {
        System.out.println(remsWeatherStation.isCalibrated());
        System.out.println(SEPARATOR);
        System.out.println("End of REMS Calibration test!");
        System.out.println(SEPARATOR);
    }
}

//http://pds-atmospheres.nmsu.edu/PDS/data/mslrem_1001/DATA/SOL_00939_01062/SOL00966
// /RME_483202550RNV09660000000_______P2.TAB
//http://pds-atmospheres.nmsu.edu/PDS/data/mslrem_1001/DATA/SOL_00939_01062/SOL00966/RME_483202550RNV9660000000_______P2.TAB