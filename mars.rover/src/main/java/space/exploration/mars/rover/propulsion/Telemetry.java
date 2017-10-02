package space.exploration.mars.rover.propulsion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.utils.RoverUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Telemetry {

    private Point                                       startLocation      = null;
    private long                                        startTimeMs        = 0l;
    private long                                        recordingFrequency = 0l;
    private List<TelemetryDataOuterClass.TelemetryData> telemetryData      = new ArrayList<>();
    private Cell                                        robot              = null;
    private ScheduledExecutorService                    telemetryRelay     = null;
    private Properties                                  marsConfig         = null;
    private Logger                                      logger             = LoggerFactory.getLogger(Telemetry.class);
    private Runnable                                    teleRelay          = new Runnable() {
        @Override
        public void run() {
            if (telemetryData.isEmpty()) {
                TelemetryDataOuterClass.TelemetryData.Builder tBuilder = TelemetryDataOuterClass.TelemetryData
                        .newBuilder();
                tBuilder.setDistanceTraveled(0);
                tBuilder.setLocation(RoverUtil.getLocation(robot.getLocation()));
                tBuilder.setHeading(0.0d);
                tBuilder.setVelocity(0.0d);
                tBuilder.setSCET(System.currentTimeMillis());
                telemetryData.add(tBuilder.build());

                startLocation = new Point((int) robot.getLocation().getX(), (int) robot.getLocation().getY());
                startTimeMs = System.currentTimeMillis();
                logger.info("Telemetry relay commenced.");
            } else {
                Point  robotLocation    = new Point((int) robot.getLocation().getX(), (int) robot.getLocation().getY());
                double distanceTraveled = robotLocation.distance(startLocation);

                TelemetryDataOuterClass.TelemetryData.Builder tBuilder = TelemetryDataOuterClass.TelemetryData
                        .newBuilder();
                int distanceScale = Integer.parseInt(marsConfig.getProperty("mars.rover.propulsion.telemetry" +
                                                                                    ".distanceScale"));
                tBuilder.setDistanceTraveled(distanceScale * distanceTraveled);
                tBuilder.setVelocity(tBuilder.getDistanceTraveled() / (double) (System.currentTimeMillis() -
                        startTimeMs));
                tBuilder.setLocation(RoverUtil.getLocation(robot.getLocation()));
                tBuilder.setHeading(RoverUtil.getHeading(startLocation, robotLocation));
                tBuilder.setSCET(System.currentTimeMillis());
                telemetryData.add(tBuilder.build());
                logger.debug(tBuilder.build().toString());
            }
        }
    };

    public Telemetry(Cell robot, Properties marsConfig) {
        this.robot = robot;
        this.recordingFrequency = Long.parseLong(marsConfig.getProperty("mars.rover.propulsion.telemetry.frequency"));
        telemetryRelay = Executors.newSingleThreadScheduledExecutor();
        this.marsConfig = marsConfig;
    }

    public void record() {
        telemetryRelay.scheduleAtFixedRate(teleRelay, 0l, recordingFrequency, TimeUnit.MILLISECONDS);
    }

    public void interrupt() {
        logger.info("Telemetry is shutdown, total number of dataPoints = " + Integer.toString(telemetryData.size()));
        telemetryRelay.shutdown();
    }

    public List<TelemetryDataOuterClass.TelemetryData> getTelemetryData() {
        return telemetryData;
    }
}
