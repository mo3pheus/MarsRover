package space.exploration.mars.rover.sensor.sam;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import space.exploration.mars.rover.bootstrap.MarsMissionLaunch;
import space.exploration.mars.rover.sensor.SamSensor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Ignore
public class SamBaseUrlCrawlTest extends TestCase {
    Properties samClibrationProperties = new Properties();

    @Before
    public void setUp() throws IOException {
        MarsMissionLaunch.configureConsoleLogging(false);
        samClibrationProperties.load(new FileInputStream(new File("src/main/resources/marsConfig.properties")));
    }

    @Test
    public void testSamSensorInitiation() throws Exception{
        SamSensor samSensor = new SamSensor(samClibrationProperties);
        if (samSensor.isCalibrated()) {
            System.out.println(samSensor);
        }
    }
}
