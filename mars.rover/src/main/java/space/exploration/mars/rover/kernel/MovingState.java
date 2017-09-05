/**
 *
 */
package space.exploration.mars.rover.kernel;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.InstructionPayloadOuterClass;
import space.exploration.mars.rover.communication.RoverStatusOuterClass.RoverStatus;
import space.exploration.mars.rover.communication.RoverStatusOuterClass.RoverStatus.Location;
import space.exploration.mars.rover.environment.EnvironmentUtils;
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
        try {
            rover.writeSystemLog(InstructionPayloadOuterClass.InstructionPayload.parseFrom(message));
        } catch (InvalidProtocolBufferException e) {
            rover.writeErrorLog("Invalid protocol", e);
        }
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

    @Override
    public String getStateName() {
        return "Moving State";
    }

    public void move(InstructionPayloadOuterClass.InstructionPayload payload) {
        MarsArchitect  architect     = rover.getMarsArchitect();
        java.awt.Point robotPosition = rover.getMarsArchitect().getRobot().getLocation();
        RobotPositions positions     = null;
        try {
            positions = RobotPositions.parseFrom(payload.getTargetsList().get(0).getAuxiliaryData().toByteArray());
        } catch (InvalidProtocolBufferException e) {
            logger.error("Invalid / corrupted move command.", e);
            rover.writeErrorLog("Invalid / corrupted move command.", e);
            rover.setState(rover.getListeningState());
            return;
        }

        Point destination = positions.getPositions(0);
        logger.debug("In moving state, destination demanded = " + destination.toString());
        if (!isDestinationValid(new java.awt.Point(destination.getX(), destination.getY()))) {
            logger.error("Destination passed in is invalid - intersects with a wall! " + destination.toString());
            rover.writeErrorLog("Destination passed in is invalid - intersects with a wall! " + destination
                    .toString(), null);
            sendFailureToEarth(rover.getMarsArchitect().getRobot().getLocation(), destination, "Destination passed in" +
                    " is invalid.");
            return;
        }

        PropulsionUnit powerTran = new PropulsionUnit(rover, robotPosition, new java.awt.Point(destination.getX(),
                                                                                               destination.getY()));

        if (!powerTran.isTrajectoryValid()) {
            logger.error("No route found between robot current position at " + rover.getMarsArchitect().getRobot()
                    .getLocation().toString() + " and " + destination.toString());
            rover.writeErrorLog("No route found between robot current position at " + rover.getMarsArchitect()
                    .getRobot()
                    .getLocation().toString() + " and " + destination.toString(), null);
            sendFailureToEarth(robotPosition, destination, "Rover was unable to find path between supplied source" +
                    " and destination.");
            return;
        } else if (!rover.getBattery().requestPower(powerTran.getPowerConsumptionPerUnit() * powerTran.getTrajectory
                ().size(), false)) {
            logger.error("Propulsion unavailable due to insufficient battery. Sending the rover into hibernating " +
                                 "state. Will attempt to restore propulsion upon battery recharge.");
            rover.writeErrorLog("Propulsion unavailable due to insufficient battery. Sending the rover into " +
                                        "hibernating " +
                                        "state. Will attempt to restore propulsion upon battery recharge.", null);
            rover.getInstructionQueue().add(payload.toByteArray());
            rover.setState(rover.getHibernatingState());
            rover.getMarsArchitect().getRobot().setColor(EnvironmentUtils.findColor("robotHibernate"));
            rover.setInRechargingModeTime(System.currentTimeMillis());
            return;
        }

        architect.updateRobotPositions(TrackingAnimationUtil.getAnimationCalibratedRobotPath(powerTran
                                                                                                     .getTrajectory
                                                                                                             (),
                                                                                             architect
                                                                                                     .getRobotStepSize()));
        architect.returnSurfaceToNormal();
        sendUpdateToEarth();
    }

    private boolean isDestinationValid(java.awt.Point destination) {
        boolean     invalid     = false;
        WallBuilder wallBuilder = new WallBuilder(rover.getMarsConfig());
        for (Wall wall : wallBuilder.getWalls()) {
            invalid = (invalid || wall.intersects(destination));
        }

        return !invalid;
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

    private void sendFailureToEarth(java.awt.Point source, Point destination, String message) {
        RoverStatus.Builder updateMsg = RoverStatus.newBuilder();
        updateMsg.setSCET(System.currentTimeMillis());
        Location robotLocation = Location.newBuilder().setX(rover.getMarsArchitect().getRobot().getLocation().x)
                .setY(rover.getMarsArchitect().getRobot().getLocation().y).build();
        updateMsg.setLocation(robotLocation);
        updateMsg.setNotes(message);
        updateMsg.setBatteryLevel(rover.getBattery().getPrimaryPowerUnits());
        updateMsg.setModuleReporting(Module.PROPULSION.getValue());
        updateMsg.setSolNumber(rover.getSol());

        rover.state = rover.transmittingState;
        rover.transmitMessage(updateMsg.build().toByteArray());
    }
}
