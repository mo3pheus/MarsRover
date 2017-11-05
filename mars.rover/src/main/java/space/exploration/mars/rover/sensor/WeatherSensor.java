package space.exploration.mars.rover.sensor;

import communications.protocol.ModuleDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.communication.RoverStatusOuterClass;
import space.exploration.communications.protocol.service.SeasonalWeather;
import space.exploration.communications.protocol.service.WeatherData;
import space.exploration.communications.protocol.service.WeatherQueryOuterClass;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.mars.rover.kernel.Rover;
import space.exploration.mars.rover.service.WeatherQueryService;
import space.exploration.mars.rover.utils.RoverUtil;
import space.exploration.mars.rover.utils.WeatherUtil;

import java.util.concurrent.TimeUnit;

/**
 * Created by skorgao on 10/10/2017.
 */
public class WeatherSensor implements IsEquipment {

    private static final String              WEATHER_SENSOR_LIFESPAN = "mars.rover.weather.station.lifeSpan";
    private              Logger              logger                  = LoggerFactory.getLogger(WeatherSensor.class);
    private              WeatherQueryService rems                    = null;
    private              int                 fullLifeSpan            = 0;
    private              int                 lifeSpan                = 0;
    private              Rover               rover                   = null;
    private              double              queryRate               = 0.0d;
    private              long                createTimeStamp         = System.currentTimeMillis();

    public WeatherSensor(Rover rover) {
        this.rover = rover;
        this.lifeSpan = Integer.parseInt(rover.getMarsConfig().getProperty(WEATHER_SENSOR_LIFESPAN));
        this.fullLifeSpan = lifeSpan;
        rems = new WeatherQueryService();
    }

    public byte[] getWeather() {
        lifeSpan--;
        RoverStatusOuterClass.RoverStatus.Builder rBuilder       = getGeneralRoverStatus();
        WeatherData.WeatherPayload                weatherPayload = null;

        try {
            /*rems -> RoverEnvironmentalMonitoringStation */
            rems.executeQuery();
            weatherPayload = (WeatherData.WeatherPayload) rems.getResponse();
        } catch (Exception e) {
            logger.error("Weather Service had an exception.", e);
        }

        if (weatherPayload != null) {
            rBuilder.setModuleMessage(weatherPayload.toByteString());
            rBuilder.setNotes("Curiosity Actual");
        } else {
            rBuilder.setNotes("No weather data at this time");
        }

        double hoursElapsed = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - createTimeStamp);
        queryRate = (hoursElapsed > 0) ? (double) (fullLifeSpan - lifeSpan) / hoursElapsed : 0.0d;
        logger.info("Current weatherQueryRate/hour = " + queryRate + " max allowed = 1000.0/hr");


        return rBuilder.build().toByteArray();
    }

    public byte[] getWeather(WeatherQueryOuterClass.WeatherQuery weatherQuery) {
        logger.debug(weatherQuery.toString());
        lifeSpan--;
        RoverStatusOuterClass.RoverStatus.Builder rBuilder               = getGeneralRoverStatus();
        SeasonalWeather.SeasonalWeatherPayload    seasonalWeatherPayload = null;

        try {
            /*rems -> RoverEnvironmentalMonitoringStation */
            if (weatherQuery.getTerrestrialEndDate() != null && weatherQuery.getTerrestrialStartDate() != null) {
                rems.setEarthEndDate(weatherQuery.getTerrestrialEndDate());
                rems.setEarthStartDate(weatherQuery.getTerrestrialStartDate());
            } else if (weatherQuery.getSolNumber() != -1) {
                rems.setSolNumber(weatherQuery.getSolNumber());
            }

            rems.executeQuery();
            seasonalWeatherPayload = WeatherUtil.getSeasonalWeatherPayload(rems.getResponseAsString());
        } catch (Exception e) {
            logger.error("Weather Service had an exception.", e);
        }

        if (seasonalWeatherPayload != null) {
            rBuilder.setModuleMessage(seasonalWeatherPayload.toByteString());
            rBuilder.setNotes("Curiosity Actual");
        } else {
            rBuilder.setNotes("No weather data at this time");
        }

        double hoursElapsed = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - createTimeStamp);
        queryRate = (hoursElapsed > 0) ? (double) (fullLifeSpan - lifeSpan) / hoursElapsed : 0.0d;
        logger.info("Current weatherQueryRate/hour = " + queryRate + " max allowed = 1000.0/hr");


        return rBuilder.build().toByteArray();
    }


    public double getQueryRate() {
        return queryRate;
    }

    @Override
    public int getLifeSpan() {
        return lifeSpan;
    }

    @Override
    public String getEquipmentName() {
        return "Mars Weather Station";
    }

    @Override
    public boolean isEndOfLife() {
        return (lifeSpan <= 0);
    }

    private RoverStatusOuterClass.RoverStatus.Builder getGeneralRoverStatus() {
        RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverStatusOuterClass.RoverStatus.newBuilder();
        rBuilder.setModuleReporting(ModuleDirectory.Module.WEATHER_SENSOR.getValue());
        rBuilder.setSCET(System.currentTimeMillis());
        rBuilder.setLocation(RoverUtil.getLocation(rover.getMarsArchitect().getRobot().getLocation()));
        rBuilder.setBatteryLevel(rover.getBattery().getPrimaryPowerUnits());
        return rBuilder;
    }
}