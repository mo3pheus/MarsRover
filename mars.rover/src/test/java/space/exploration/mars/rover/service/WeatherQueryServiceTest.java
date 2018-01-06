package space.exploration.mars.rover.service;

import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;
import space.exploration.communications.protocol.service.WeatherData;
import space.exploration.mars.rover.bootstrap.MarsMissionLaunch;

@Ignore
public class WeatherQueryServiceTest extends TestCase {
    private WeatherQueryService weatherQueryService = null;

    @Override
    public void setUp() {
        MarsMissionLaunch.configureLogging(true);
        weatherQueryService = new WeatherQueryService();
        weatherQueryService.setSolNumber(1212);
        weatherQueryService.executeQuery();
    }

    @Test
    public void testPayloadObject() {
        WeatherData.WeatherPayload weatherPayload = (WeatherData.WeatherPayload) weatherQueryService.getResponse();
        System.out.println(weatherPayload);
    }
}
