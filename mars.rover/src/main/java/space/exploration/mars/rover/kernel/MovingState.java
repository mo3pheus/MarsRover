/**
 *
 */
package space.exploration.mars.rover.kernel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.communication.RoverStatusOuterClass.RoverStatus;
import space.exploration.mars.rover.communication.RoverStatusOuterClass.RoverStatus.Location;
import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.environment.Wall;
import space.exploration.mars.rover.environment.WallBuilder;
import space.exploration.mars.rover.kernel.ModuleDirectory.Module;
import space.exploration.mars.rover.propulsion.PropulsionUnit;
import space.exploration.mars.rover.robot.RobotPositionsOuterClass.RobotPositions;
import space.exploration.mars.rover.robot.RobotPositionsOuterClass.RobotPositions.Point;
import space.exploration.mars.rover.utils.TrackingAnimationUtil;

/**
 * @author sanketkorgaonkar
 */
public class MovingState implements State {

    private Rover  rover  = null;
    private Logger logger = LoggerFactory.getLogger(MovingState.class);

    public MovingState(Rover rover) {
        this.rover = rover;
    }

    public void receiveMessage(byte[] message) {
        rover.getInstructionQueue().add(message);
    }

    public void transmitMessage(byte[] message) {
    }

    public void exploreArea() {
    }

    public void activateCamera() {
    }

    public void hibernate() {
    }

    public void rechargeBattery() {
    }

    public void scanSurroundings() {
    }

    public void performDiagnostics() {
    }

    @Override
    public void performRadarScan() {

    }

    public void move(RobotPositions positions) {
        MarsArchitect  architect     = rover.getMarsArchitect();
        java.awt.Point robotPosition = rover.getMarsArchitect().getRobot().getLocation();

        Point destination = positions.getPositions(0);
        if (!isDestinationValid(new java.awt.Point(robotPosition.x, robotPosition.y))) {
            logger.error("Destination passed in is invalid - intersects with a wall! " + destination.toString());
            return;
        }

        PropulsionUnit powerTran = new PropulsionUnit(rover, robotPosition, new java.awt.Point(destination.getX(),
                                                                                               destination.getY()));

        architect.updateRobotPositions(TrackingAnimationUtil.getAnimationCalibratedRobotPath(powerTran.getTrajectory
                (), architect.getRobotStepSize()));

        architect.returnSurfaceToNormal();
        sendUpdateToEarth();
    }

    private boolean isDestinationValid(java.awt.Point destination) {
        boolean     valid       = false;
        WallBuilder wallBuilder = new WallBuilder(rover.getMarsConfig());
        for (Wall wall : wallBuilder.getWalls()) {
            valid = (valid || wall.intersects(destination));
        }

        return valid;
    }

    private void sendUpdateToEarth() {
        RoverStatus.Builder updateMsg = RoverStatus.newBuilder();
        updateMsg.setSCET(System.currentTimeMillis());
        Location robotLocation = Location.newBuilder().setX(rover.getMarsArchitect().getRobot().getLocation().x)
                .setY(rover.getMarsArchitect().getRobot().getLocation().y).build();
        updateMsg.setLocation(robotLocation);
        updateMsg.setNotes(
                "Rover has moved from its previous location. Check subsequent lidarMsg for an environment update.");
        updateMsg.setBatteryLevel(rover.getBattery().getPrimaryPowerUnits());
        updateMsg.setModuleReporting(Module.PROPULSION.getValue());
        updateMsg.setSolNumber(rover.getSol());

        rover.state = rover.transmittingState;
        rover.transmitMessage(updateMsg.build().toByteArray());
    }
}
