package space.exploration.mars.rover.kernel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.animation.RadarAnimationEngine;
import space.exploration.mars.rover.communication.RoverStatusOuterClass;
import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.environment.RadarContactCell;
import space.exploration.mars.rover.environment.RoverCell;
import space.exploration.mars.rover.robot.RobotPositionsOuterClass;
import space.exploration.mars.rover.sensor.Radar;
import space.exploration.mars.rover.utils.RoverUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

/**
 * Created by sanket on 5/30/17.
 */
public class RadarScanningState implements State {
    private Rover  rover  = null;
    private Logger logger = LoggerFactory.getLogger(RadarScanningState.class);

    public RadarScanningState(Rover rover) {
        this.rover = rover;
    }

    @Override
    public void receiveMessage(byte[] message) {
        rover.getInstructionQueue().add(message);
    }

    @Override
    public void transmitMessage(byte[] message) {
        RoverUtil.roverSystemLog(logger, " Invalid action error. Can not transmit in current state.", "ERROR");
    }

    @Override
    public void exploreArea() {
        RoverUtil.roverSystemLog(logger, " Invalid action error. Can not explore in current state.", "ERROR");
    }

    @Override
    public void activateCamera() {
        RoverUtil.roverSystemLog(logger, " Invalid action error. Can not click photos in current state.", "ERROR");
    }

    @Override
    public void move(RobotPositionsOuterClass.RobotPositions positions) {
        RoverUtil.roverSystemLog(logger, " Invalid action error. Can not move in current state.", "ERROR");
    }

    @Override
    public void hibernate() {
        RoverUtil.roverSystemLog(logger, " Invalid action error. Can not hibernate in current state.", "ERROR");
    }

    @Override
    public void rechargeBattery() {
        RoverUtil.roverSystemLog(logger, " Invalid action error. Can not rechargeBattery in current state.", "ERROR");
    }

    @Override
    public void scanSurroundings() {
        RoverUtil.roverSystemLog(logger, " Invalid action error. Can not scanSurroundings in current state.", "ERROR");
    }

    @Override
    public void performDiagnostics() {
        RoverUtil.roverSystemLog(logger, " Invalid action error. Can not performDiagnostics in current state.",
                "ERROR");
    }

    @Override
    public void performRadarScan() {
        logger.info("Performing Radar Scan, current position = " + rover.getMarsArchitect().getRobot().getLocation()
                .toString());
        MarsArchitect marsArchitect = rover.getMarsArchitect();
        renderRadarAnimation();

        RoverStatusOuterClass.RoverStatus.Location location = RoverStatusOuterClass.RoverStatus.Location
                .newBuilder()
                .setX(marsArchitect.getRobot().getLocation().x)
                .setY(marsArchitect.getRobot().getLocation().y).build();
        RoverStatusOuterClass.RoverStatus         status   = null;
        RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverStatusOuterClass.RoverStatus.newBuilder();
        status = rBuilder.setBatteryLevel(rover.getBattery()
                .getPrimaryPowerUnits())
                .setSolNumber(rover.getSol()).setLocation(location).setNotes("Radar scan performed!")
                .setSCET(System
                        .currentTimeMillis())
                .setModuleReporting(ModuleDirectory.Module.RADAR.getValue()).build();
        rover.state = rover.transmittingState;
        rover.transmitMessage(status.toByteArray());
    }

    private void renderRadarAnimation() {
        Properties marsConfig = rover.getMarsConfig();
        double scaleFactor = Double.parseDouble(marsConfig.getProperty(Radar.RADAR_PREFIX + "" +
                                                                       ".scaleFactor"));
        RadarAnimationEngine             radarAnimationEngine = new RadarAnimationEngine(marsConfig);
        Point                            origin               = radarAnimationEngine.getOrigin();
        Map<Point, RoverCell>            oldRovers            = rover.getPreviousRovers();
        java.util.List<RadarContactCell> contacts             = new ArrayList<>();
        for (Point p : oldRovers.keySet()) {
//            double dist  = scaleFactor * Math.sqrt(Math.pow((p.x), 2.0d) + Math.pow((p.y), 2.0d));
//            double slope = (origin.y - p.y) / (double) (origin.x - p.x);
//            double theta = Math.atan(slope);
//            Point scaledPoint = new Point((int) (dist * Math.cos(theta)) + origin.x, (int) (dist * Math.sin(theta))
//                                                                                     + origin.y);
//            int cellWidth = rover.getMarsArchitect().getCellWidth();
//            Point adjustedPoint = new Point(scaledPoint.x - (scaledPoint.x % cellWidth), scaledPoint.y -
//                                                                                         (scaledPoint.y % cellWidth));
//            contacts.add(new RadarContactCell(marsConfig, adjustedPoint, Color.green));
//            System.out.println("Original point = " + p.toString());
//            System.out.println("Scaled point = " + scaledPoint.toString());
//            System.out.println("Adjusted point = " + adjustedPoint.toString());
            contacts.add(new RadarContactCell(marsConfig, p, Color.green, 8));
        }
        radarAnimationEngine.setContacts(contacts);
        radarAnimationEngine.renderLaserAnimation();
        radarAnimationEngine.destroy();
    }
}
