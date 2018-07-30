package space.exploration.mars.rover.sensor.sam;

import junit.framework.TestCase;
import org.junit.Ignore;
import space.exploration.mars.rover.sensor.SamSensor;

@Ignore
public class SamBaseUrlCrawlTest extends TestCase {
    @Ignore
    public void testSamSensorInitiation() {
        SamSensor samSensor = new SamSensor(null);
        if (samSensor.isCalibrated()) {
            System.out.println(samSensor);
        }
    }
}
