package space.exploration.mars.rover.sensor;

import com.google.protobuf.ByteString;
import communications.protocol.ModuleDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.communication.RoverStatusOuterClass;
import space.exploration.communications.protocol.service.WeatherRDRData;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.mars.rover.kernel.Rover;
import space.exploration.mars.rover.service.WeatherDataService;
import space.exploration.mars.rover.utils.RoverUtil;
import space.exploration.mars.rover.utils.WeatherUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by skorgao on 10/10/2017.
 */
public class WeatherSensor implements IsEquipment {
    private static final String WEATHER_SENSOR_LIFESPAN    = "mars.rover.weather.station.lifeSpan";
    private static final Long   STALE_DATA_THRESHOLD_HOURS = 19l;
    private              Logger logger                     = LoggerFactory.getLogger(WeatherSensor.class);

    private volatile Map<Double, WeatherRDRData.WeatherEnvReducedData> weatherEnvReducedDataMap = null;
    private          WeatherDataService                                weatherDataService       = null;
    private          int                                               fullLifeSpan             = 0;
    private          int                                               lifeSpan                 = 0;
    private          Rover                                             rover                    = null;
    private          double                                            queryRate                = 0.0d;
    private          long                                              createTimeStamp          = System
            .currentTimeMillis();
    private volatile boolean                                           calibratingSensor        = false;
    private          ExecutorService                                   calibrationService       = null;

    public WeatherSensor(Rover rover) {
        this.rover = rover;
        this.lifeSpan = Integer.parseInt(rover.getMarsConfig().getProperty(WEATHER_SENSOR_LIFESPAN));
        this.fullLifeSpan = lifeSpan;
        this.weatherEnvReducedDataMap = new HashMap<>();
        this.weatherDataService = new WeatherDataService();
        calibrationService = Executors.newSingleThreadScheduledExecutor();
    }

    public boolean isCalibratingSensor() {
        return calibratingSensor;
    }

    public void calibrateREMS(int sol) {
        SensorCalibrater sensorCalibrater = new SensorCalibrater(sol);
        calibrationService.submit(sensorCalibrater);
    }

    public byte[] getWeather() {
        lifeSpan--;
        RoverStatusOuterClass.RoverStatus.Builder rBuilder              = getGeneralRoverStatus();
        Double                                    currentEphemerisTime  = rover.getSpacecraftClock().getEphemerisTime();
        WeatherRDRData.WeatherEnvReducedData      weatherEnvReducedData = findClosestWeatherData(currentEphemerisTime);
        if (weatherEnvReducedData != null) {
            rBuilder.setModuleMessage(ByteString.copyFrom(findClosestWeatherData(currentEphemerisTime).toByteArray()));
        } else {
            rBuilder.setNotes("Weather Data not available at this time. Please calibrate the weatherSensor." +
                                      " Calendar Time = " + rover.getSpacecraftClock().getCalendarTime());
        }

        return rBuilder.build().toByteArray();
    }

    private WeatherRDRData.WeatherEnvReducedData findClosestWeatherData(Double epemerisTime) {
        if (weatherEnvReducedDataMap.containsKey(epemerisTime)) {
            return weatherEnvReducedDataMap.get(epemerisTime);
        }

        Double                               timeDiff              = Double.MAX_VALUE;
        WeatherRDRData.WeatherEnvReducedData weatherEnvReducedData = null;
        for (Double ephTime : weatherEnvReducedDataMap.keySet()) {
            Double diff = Math.abs(epemerisTime - ephTime);
            if (diff < timeDiff) {
                timeDiff = diff;
                weatherEnvReducedData = weatherEnvReducedDataMap.get(ephTime);
            }
        }

        /* Check if the difference is more than 19 hours */
        if (timeDiff >= TimeUnit.HOURS.toSeconds(STALE_DATA_THRESHOLD_HOURS)) {
            logger.error("WeatherData is too stale. Calendar Date = " + rover.getSpacecraftClock().getCalendarTime());
            return null;
        }

        return weatherEnvReducedData;
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

    @Override
    public long getRequestMetric() {
        return rover.getWeatherSensingState().getRequests().count();
    }

    private RoverStatusOuterClass.RoverStatus.Builder getGeneralRoverStatus() {
        RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverStatusOuterClass.RoverStatus.newBuilder();
        rBuilder.setModuleReporting(ModuleDirectory.Module.WEATHER_SENSOR.getValue());
        rBuilder.setSCET(rover.getSpacecraftClock().getInternalClock().getMillis());
        rBuilder.setSolNumber(rover.getSpacecraftClock().getSol());
        rBuilder.setLocation(RoverUtil.getLocation(rover.getMarsArchitect().getRobot().getLocation()));
        rBuilder.setBatteryLevel(rover.getBattery().getPrimaryPowerUnits());
        return rBuilder;
    }

    private class SensorCalibrater implements Runnable {

        private int sol = 0;

        public SensorCalibrater(int sol) {
            this.sol = sol;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("remsCalibration");
            logger.info("Calibrating REMS Weather Station now for sol = " + this.sol);
            calibratingSensor = true;
            weatherDataService.downloadCalibrationData(this.sol);
            File weatherDataFile = weatherDataService.getWeatherCalibrationFile();

            logger.info("CalibrationStatus for REMS = " + Boolean.toString(weatherDataService.isCalibrated()));
            weatherEnvReducedDataMap = WeatherUtil.readWeatherDataFile(weatherDataFile);
            weatherDataFile.delete();

            calibratingSensor = false;
        }
    }
}