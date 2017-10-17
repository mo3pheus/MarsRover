package space.exploration.mars.rover.dataUplink;

import junit.framework.TestCase;
import org.junit.Test;
import space.exploration.mars.rover.bootstrap.MarsMissionLaunch;

public class WeatherQueryServiceTest extends TestCase {
    private WeatherQueryService weatherQueryService = null;

    @Override
    public void setUp() {
        MarsMissionLaunch.configureLogging();
        weatherQueryService = new WeatherQueryService();
        weatherQueryService.executeQuery();
    }

    @Test
    public void testPayloadObject(){
        WeatherData.WeatherPayload weatherPayload = (WeatherData.WeatherPayload)weatherQueryService.getResponse();
        System.out.println(weatherPayload);
    }
}
