package space.exploration.mars.rover.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.propulsion.TelemetryDataOuterClass;
import space.exploration.communications.protocol.propulsion.TelemetryPacketOuterClass;
import space.exploration.mars.rover.animation.TrackingAnimationEngine;
import space.exploration.mars.rover.utils.RoverUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class TelemetrySensor implements Observer {
    public static final String                                      TELEMETRY_DISTANCE_SCALE     = "mars.rover" +
            ".propulsion.telemetry.distanceScale";
    public static final String                                      TELEMETRY_COMPRESSION_FACTOR = "mars.rover" +
            ".propulsion.telemetry.compressionFactor";
    private             Point                                       startLocation                = null;
    private             long                                        startTimeMs                  = 0l;
    private             List<TelemetryDataOuterClass.TelemetryData> telemetryData                = new ArrayList<>();
    private             Logger                                      logger                       = LoggerFactory
            .getLogger(TelemetrySensor.class);
    private             TelemetryPacketOuterClass.TelemetryPacket   telemetryPacket              = null;
    private             TrackingAnimationEngine                     propulsionEngine             = null;
    private             int                                         telemetryCompressionFactor   = 1;

    public TelemetrySensor(Observable observable) {
        observable.addObserver(this);
        if (observable instanceof TrackingAnimationEngine) {
            propulsionEngine           = (TrackingAnimationEngine) observable;
            telemetryCompressionFactor = Integer.parseInt(propulsionEngine.getMarsRoverConfig().getProperty
                    (TELEMETRY_COMPRESSION_FACTOR));
        }
    }

    @Override
    public void update(Observable observable, Object endOfMovement) {
        if (observable instanceof TrackingAnimationEngine) {
            if ((Boolean) endOfMovement) {
                buildTelemetryPacket();
                logger.info("Telemetry relay stopped, number of telemetryDataPoints = " + telemetryPacket
                        .getTelemetryDataCount());
                telemetryData.clear();
                return;
            }

            if (telemetryData.isEmpty()) {
                TelemetryDataOuterClass.TelemetryData.Builder tBuilder = TelemetryDataOuterClass.TelemetryData
                        .newBuilder();
                tBuilder.setDistanceTraveled(0);
                tBuilder.setLocation(RoverUtil.getLocation(propulsionEngine.getRobot().getLocation()));
                tBuilder.setHeading(0.0d);
                tBuilder.setVelocity(0.0d);
                tBuilder.setSCET(System.currentTimeMillis());
                telemetryData.add(tBuilder.build());

                startLocation = new Point((int) propulsionEngine.getRobot().getLocation().getX(), (int)
                        propulsionEngine.getRobot().getLocation().getY());
                startTimeMs   = System.currentTimeMillis();
                logger.info("Telemetry relay commenced.");
            } else {
                Point robotLocation = new Point((int) propulsionEngine.getRobot().getLocation().getX(), (int)
                        propulsionEngine.getRobot().getLocation().getY());
                double distanceTraveled = robotLocation.distance(startLocation);

                TelemetryDataOuterClass.TelemetryData.Builder tBuilder = TelemetryDataOuterClass.TelemetryData
                        .newBuilder();
                int distanceScale = Integer.parseInt(propulsionEngine.getMarsRoverConfig().getProperty
                        (TELEMETRY_DISTANCE_SCALE));
                tBuilder.setDistanceTraveled(distanceScale * distanceTraveled);
                tBuilder.setVelocity(tBuilder.getDistanceTraveled() / (double) (System.currentTimeMillis() -
                        startTimeMs));
                tBuilder.setLocation(RoverUtil.getLocation(propulsionEngine.getRobot().getLocation()));
                tBuilder.setHeading(RoverUtil.getHeading(startLocation, robotLocation));
                tBuilder.setSCET(System.currentTimeMillis());
                telemetryData.add(tBuilder.build());
                logger.debug(tBuilder.build().toString());
            }
        }
    }

    private void buildTelemetryPacket() {
        TelemetryPacketOuterClass.TelemetryPacket.Builder tPacketBuilder = TelemetryPacketOuterClass.TelemetryPacket
                .newBuilder();

        for (int i = 0; i < telemetryData.size(); i++) {
            if ((i % telemetryCompressionFactor) == 0) {
                tPacketBuilder.addTelemetryData(telemetryData.get(i));
            }
        }
        telemetryPacket = tPacketBuilder.build();
    }

    public TelemetryPacketOuterClass.TelemetryPacket getTelemetryPacket() {
        return telemetryPacket;
    }
}
