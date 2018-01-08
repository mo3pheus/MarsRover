package space.exploration.mars.rover.service;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;

import static space.exploration.mars.rover.service.WeatherDataService.REMS_CALIBRATION_FILE;

public class WeatherDataTest extends TestCase {

    private WeatherDataService weatherDataService = new WeatherDataService();

    @Test
    public void testWeatherDataResponse() throws IOException {
        FileOutputStream fos = new FileOutputStream(REMS_CALIBRATION_FILE);
        fos.write(weatherDataService.download("http://pds-atmospheres.nmsu" +
                                                      ".edu/PDS/data/mslrem_1001/DATA/SOL_00708_00804/SOL00769/RME_465713992RNV07690000000_______P3.TAB"));
        fos.close();
    }
}
