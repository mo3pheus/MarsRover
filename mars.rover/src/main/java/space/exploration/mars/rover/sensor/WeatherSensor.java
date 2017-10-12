package space.exploration.mars.rover.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.communication.RoverStatusOuterClass;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.mars.rover.kernel.ModuleDirectory;
import space.exploration.mars.rover.kernel.Rover;
import space.exploration.mars.rover.utils.RoverUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by skorgao on 10/10/2017.
 */
public class WeatherSensor implements IsEquipment {

    private static final String WEATHER_URL_PROPERTY    = "mars.rover.weather.station.url";
    private static final String WEATHER_SENSOR_LIFESPAN = "mars.rover.weather.station.lifeSpan";
    private              String url                     = null;
    private              Logger logger                  = LoggerFactory.getLogger(WeatherSensor.class);
    private              int    lifeSpan                = 0;
    private              Rover  rover                   = null;

    public WeatherSensor(Rover rover) {
        this.rover = rover;
        this.url = rover.getMarsConfig().getProperty(WEATHER_URL_PROPERTY);
        this.lifeSpan = Integer.parseInt(rover.getMarsConfig().getProperty(WEATHER_SENSOR_LIFESPAN));
    }

    public byte[] getWeather() {
        BufferedReader                            in       = null;
        RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverStatusOuterClass.RoverStatus.newBuilder();
        try {
            URL urlObj = new URL(url);
            HttpURLConnection dataLink = (HttpURLConnection) urlObj
                    .openConnection();
            dataLink.setRequestMethod("GET");
            int responseCode = dataLink.getResponseCode();

            logger.info("Response from weather service = " + Integer.toString(responseCode));
            logger.info("Content type is = " + dataLink.getContentType());

            in = new BufferedReader(new InputStreamReader(dataLink.getInputStream()));
            String       line     = null;
            StringBuffer response = new StringBuffer();
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            rBuilder.setSCET(System.currentTimeMillis());
            rBuilder.setSolNumber(rover.getSol());
            rBuilder.setLocation(RoverUtil.getLocation(rover.getMarsArchitect().getRobot().getLocation()));
            rBuilder.setModuleReporting(ModuleDirectory.Module.WEATHER_SENSOR.getValue());
            rBuilder.setNotes(response.toString());
            rBuilder.setBatteryLevel(rover.getBattery().getPrimaryPowerUnits());

            lifeSpan--;
        } catch (MalformedURLException e) {
            logger.error("Malformed url ", e);
        } catch (IOException e) {
            logger.error("IOException ", e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                logger.error("IOException", e);
            }
        }
        return rBuilder.build().toByteArray();
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
}