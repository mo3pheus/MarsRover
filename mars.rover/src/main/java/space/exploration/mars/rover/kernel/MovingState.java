/**
 *
 */
package space.exploration.mars.rover.kernel;

import com.google.protobuf.InvalidProtocolBufferException;
import com.yammer.metrics.core.Meter;
import communications.protocol.ModuleDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.InstructionPayloadOuterClass;
import space.exploration.communications.protocol.communication.RoverStatusOuterClass;
import space.exploration.communications.protocol.robot.RobotPositionsOuterClass.RobotPositions;
import space.exploration.communications.protocol.service.WeatherQueryOuterClass;
import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.environment.Wall;
import space.exploration.mars.rover.environment.WallBuilder;
import space.exploration.mars.rover.propulsion.AStarPropulsionUnit;
import space.exploration.mars.rover.propulsion.LearningPropulsionUnit;
import space.exploration.mars.rover.propulsion.PropulsionUnit;
import space.exploration.mars.rover.utils.TrackingAnimationUtil;

import java.util.concurrent.TimeUnit;

import static space.exploration.mars.rover.kernel.Rover.PROPULSION_CHOICE;

/**
 * @author sanketkorgaonkar
 */
public class MovingState implements State {
    private             Meter  requests          = null;
    private             Rover  rover             = null;
    private             Logger logger            = LoggerFactory.getLogger(MovingState.class);

    public MovingState(Rover rover) {
        this.rover = rover;
        requests = this.rover.getMetrics().newMeter(MovingState.class, getStateName(), "requests", TimeUnit.HOURS);
    }

    @Override
    public void activateCameraById(String camId) {

    }

    @Override
    public Meter getRequests() {
        return requests;
    }

    @Override
    public void gracefulShutdown() {
        logger.error(" Can not perform gracefulShutdown while in " + getStateName());
    }

    @Override
    public void synchronizeClocks(String utcTime) {
        logger.debug("Can not sync clocks in " + getStateName());
    }

    public void receiveMessage(byte[] message) {
        rover.getInstructionQueue().add(message);
        try {
            rover.writeSystemLog(InstructionPayloadOuterClass.InstructionPayload.parseFrom(message), rover
                    .getInstructionQueue().size());
        } catch (InvalidProtocolBufferException e) {
            rover.writeErrorLog("Invalid protocol", e);
        }
    }

    public void transmitMessage(byte[] message) {
    }

    public void exploreArea() {
    }

    public void hibernate() {
    }

    public void senseWeather(WeatherQueryOuterClass.WeatherQuery weatherQuery) {
    }

    public void scanSurroundings() {
    }

    public void activateCameraById() {
    }

    @Override
    public void performRadarScan() {

    }

    @Override
    public void sleep() {

    }

    @Override
    public void wakeUp() {

    }

    @Override
    public void getSclkInformation() {
        logger.error("Can not get sclkInformation while in movingState");
    }

    @Override
    public String getStateName() {
        return "Moving State";
    }

    public void move(InstructionPayloadOuterClass.InstructionPayload.TargetPackage targetPackage) {
        requests.mark();
        MarsArchitect  architect     = rover.getMarsArchitect();
        java.awt.Point robotPosition = rover.getMarsArchitect().getRobot().getLocation();
        RobotPositions positions     = null;
        try {
            positions = RobotPositions.parseFrom(targetPackage.getAuxiliaryData().toByteArray());
        } catch (InvalidProtocolBufferException e) {
            logger.error("Invalid / corrupted move command.", e);
            rover.writeErrorLog("Invalid / corrupted move command.", e);
            rover.setState(rover.getListeningState());
            return;
        }

        RobotPositions.Point destination = positions.getPositions(0);
        logger.debug("In moving state, destination demanded = " + destination.toString());
        if (!isDestinationValid(new java.awt.Point(destination.getX(), destination.getY()))) {
            logger.error("Destination passed in is invalid - intersects with a wall! " + destination.toString());
            rover.writeErrorLog("Destination passed in is invalid - intersects with a wall! " + destination
                    .toString(), null);
            sendFailureToEarth(rover.getMarsArchitect().getRobot().getLocation(), destination, "Destination passed in" +
                    " is invalid.");
            return;
        }

        String propulsionChoice = rover.getMarsConfig().getProperty(PROPULSION_CHOICE);
        AStarPropulsionUnit aStarPropulsionUnit = new AStarPropulsionUnit(rover, robotPosition, new java.awt.Point
                (destination.getX(),
                                                                                                                   destination.getY()));
        LearningPropulsionUnit learningPropulsionUnit = new LearningPropulsionUnit(rover, robotPosition, new java.awt
                .Point(destination.getX(),
                                                                                                                            destination.getY()));

        PropulsionUnit powerTran = (propulsionChoice.equals("rl")) ? learningPropulsionUnit : aStarPropulsionUnit;
        if (!powerTran.isTrajectoryValid()) {
            logger.error("No route found between robot current position at " + rover.getMarsArchitect().getRobot()
                    .getLocation().toString() + " and " + destination.toString());
            rover.writeErrorLog("No route found between robot current position at " + rover.getMarsArchitect()
                    .getRobot()
                    .getLocation().toString() + " and " + destination.toString(), null);
            sendFailureToEarth(robotPosition, destination, "Rover was unable to find path between supplied source" +
                    " and destination.");
            return;
        }

        architect.updateRobotPositions(TrackingAnimationUtil.getAnimationCalibratedRobotPath(powerTran
                                                                                                     .getTrajectory
                                                                                                             (),
                                                                                             architect
                                                                                                     .getRobotStepSize()));
        architect.returnSurfaceToNormal();
        sendTelemetry(architect);
    }

    /**
     * Should also check if the point falls outside the grid.
     *
     * @param destination
     * @return
     */
    private boolean isDestinationValid(java.awt.Point destination) {
        boolean     invalid     = false;
        WallBuilder wallBuilder = new WallBuilder(rover.getMarsConfig());
        for (Wall wall : wallBuilder.getWalls()) {
            invalid = (invalid || wall.intersects(destination));
        }

        return !invalid;
    }

    private void sendTelemetry(MarsArchitect architect) {
        RoverStatusOuterClass.RoverStatus.Builder updateMsg = RoverStatusOuterClass.RoverStatus.newBuilder();
        updateMsg.setSCET(System.currentTimeMillis());
        RoverStatusOuterClass.RoverStatus.Location robotLocation = RoverStatusOuterClass.RoverStatus.Location
                .newBuilder().setX(rover.getMarsArchitect().getRobot().getLocation().x)
                .setY(rover.getMarsArchitect().getRobot().getLocation().y).build();
        updateMsg.setLocation(robotLocation);
        updateMsg.setNotes(
                "Telemetry Data relayed.");
        updateMsg.setBatteryLevel(rover.getBattery().getPrimaryPowerUnits());
        updateMsg.setModuleReporting(ModuleDirectory.Module.PROPULSION.getValue());
        updateMsg.setSolNumber(rover.getSpacecraftClock().getSol());
        updateMsg.setModuleMessage(architect.getTelemetryPayload().toByteString());

        rover.state = rover.transmittingState;
        rover.transmitMessage(updateMsg.build().toByteArray());
    }

    private void sendFailureToEarth(java.awt.Point source, RobotPositions.Point destination, String message) {
        RoverStatusOuterClass.RoverStatus.Builder updateMsg = RoverStatusOuterClass.RoverStatus.newBuilder();
        updateMsg.setSCET(System.currentTimeMillis());
        RoverStatusOuterClass.RoverStatus.Location robotLocation = RoverStatusOuterClass.RoverStatus.Location
                .newBuilder().setX(rover.getMarsArchitect().getRobot().getLocation().x)
                .setY(rover.getMarsArchitect().getRobot().getLocation().y).build();
        updateMsg.setLocation(robotLocation);
        updateMsg.setNotes(message);
        updateMsg.setBatteryLevel(rover.getBattery().getPrimaryPowerUnits());
        updateMsg.setModuleReporting(ModuleDirectory.Module.PROPULSION.getValue());
        updateMsg.setSolNumber(rover.getSpacecraftClock().getSol());

        rover.state = rover.transmittingState;
        rover.transmitMessage(updateMsg.build().toByteArray());
    }
}
