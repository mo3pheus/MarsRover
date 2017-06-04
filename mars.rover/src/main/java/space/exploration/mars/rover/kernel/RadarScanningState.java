package space.exploration.mars.rover.kernel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.animation.RadarAnimationEngine;
import space.exploration.mars.rover.communication.RoverStatusOuterClass;
import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.robot.RobotPositionsOuterClass;
import space.exploration.mars.rover.utils.RoverUtil;

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
        MarsArchitect        marsArchitect        = rover.getMarsArchitect();
        RadarAnimationEngine radarAnimationEngine = new RadarAnimationEngine(rover.getMarsConfig());
        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        radarAnimationEngine.destroy();

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
}
