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
    public static final String LIFESPAN = "mars.rover.rems.lifespan";
    private             Logger logger   = LoggerFactory.getLogger(WeatherSensor.class);

    private          double                                            staleDataThresholdHours  = 0.0d;
    private volatile Map<Double, WeatherRDRData.WeatherEnvReducedData> weatherEnvReducedDataMap = null;
    private          WeatherDataService                                weatherDataService       = null;
    private          int                                               fullLifeSpan             = 0;
    private          int                                               lifeSpan                 = 0;
    private          Rover                                             rover                    = null;
    private          double                                            queryRate                = 0.0d;
    private volatile boolean                                           calibratingSensor        = false;
    private volatile double                                            calibrationKickOffTime   = 0.0d;
    private          ExecutorService                                   calibrationService       = Executors
            .newSingleThreadScheduledExecutor();
    private          int                                               sol                      = 0;

    public WeatherSensor(Rover rover) {
        this.rover = rover;
        this.lifeSpan = Integer.parseInt(rover.getRoverConfig().getMarsConfig().getProperty(LIFESPAN));
        this.fullLifeSpan = lifeSpan;
        this.weatherEnvReducedDataMap = new HashMap<>();
        this.weatherDataService = new WeatherDataService();
        this.staleDataThresholdHours = TimeUnit.MILLISECONDS.toHours(rover.getOneSolDuration());
    }

    public boolean isCalibratingSensor() {
        return calibratingSensor;
    }

    public void calibrateREMS(int sol) {
        this.sol = sol;
        calibrationService.submit(new CalibrationKickOff(sol));
        calibrationKickOffTime = rover.getSpacecraftClock().getEphemerisTime();
    }

    public byte[] getWeather() {
        lifeSpan--;
        RoverStatusOuterClass.RoverStatus.Builder rBuilder             = getGeneralRoverStatus();
        Double                                    currentEphemerisTime = rover.getSpacecraftClock().getEphemerisTime();

        if (calibratingSensor) {
            logger.error("REMS sensor is calibrating. Please wait for a while.");
            rBuilder.setNotes("Weather Data not available at this time. REMS Calibration is ongoing. Calibration " +
                                      "kicked off at TDB = " + calibrationKickOffTime
                                      + " Time elapsed = " + (rover.getSpacecraftClock().getEphemerisTime() -
                    calibrationKickOffTime) + " seconds"
            );
        } else {
            WeatherRDRData.WeatherEnvReducedData weatherEnvReducedData = findClosestWeatherData(currentEphemerisTime);
            if (weatherEnvReducedData != null) {
                rBuilder.setModuleMessage(ByteString.copyFrom(weatherEnvReducedData.toByteArray()));

            } else {
                rBuilder.setNotes("Weather Data not available at this time. Please calibrate the weatherSensor." +
                                          " Calendar Time = " + rover.getSpacecraftClock().getCalendarTime());
            }
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

        /* Check if the difference is more than 24 hours. This is because the weatherSensor aboard Curiosity is
        exercised for only 5 minutes a day */
        if (timeDiff >= TimeUnit.HOURS.toSeconds((long) staleDataThresholdHours)) {
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

    @Override
    public String getEquipmentLifeSpanProperty() {
        return LIFESPAN;
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

    private class CalibrationKickOff implements Runnable {
        int sol;

        CalibrationKickOff(int sol) {
            this.sol = sol;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("remsCalibrationKickOff");
            calibratingSensor = true;
            File weatherFile = weatherDataService.downloadCalibrationData(sol);
            if (weatherFile != null) {
                weatherEnvReducedDataMap = WeatherUtil.readWeatherDataFile(weatherFile);
                calibratingSensor = false;
            } else {
                logger.info("No weather data for sol = " + sol);
            }
        }
    }
}