/**
 *
 */
package space.exploration.mars.rover.kernel;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.InstructionPayloadOuterClass;
import space.exploration.mars.rover.communication.RoverPingOuterClass.RoverPing;
import space.exploration.mars.rover.communication.RoverStatusOuterClass.RoverStatus;
import space.exploration.mars.rover.communication.RoverStatusOuterClass.RoverStatus.Location;
import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.environment.WallBuilder;

/**
 * @author sanketkorgaonkar
 */
public class SensingState implements State {
    private Logger logger = LoggerFactory.getLogger(SensingState.class);
    private Rover  rover  = null;

    public SensingState(Rover rover) {
        this.rover = rover;
    }

    public void receiveMessage(byte[] message) {
        rover.getInstructionQueue().add(message);
        logger.debug("Rover not in the correct state to receive message. Message added to the instruction queue, " +
                             "instruction queue length = " + rover.getInstructionQueue().size());
        try {
            rover.writeSystemLog("Rover not in the correct state to receive message. Message added to the instruction" +
                                         " queue, " +
                                         "instruction queue length = ", rover.getInstructionQueue().size());
            rover.writeSystemLog(InstructionPayloadOuterClass.InstructionPayload.parseFrom(message), rover
                    .getInstructionQueue().size());
        } catch (InvalidProtocolBufferException ipe) {
            rover.writeErrorLog("InvalidProtocolBuffer", ipe);
        }
    }

    @Override
    public String getStateName() {
        return "Sensing State";
    }

    public void transmitMessage(byte[] message) {
        // TODO Auto-generated method stub

    }

    public void exploreArea() {
        // TODO Auto-generated method stub

    }

    public void activateCamera() {
        // TODO Auto-generated method stub

    }

    public void move(InstructionPayloadOuterClass.InstructionPayload payload) {
    }


    public void hibernate() {
        // TODO Auto-generated method stub

    }

    public void rechargeBattery() {
        // TODO Auto-generated method stub

    }

    public void scanSurroundings() {
        MarsArchitect marsArchitect = rover.getMarsArchitect();

        rover.configureLidar(marsArchitect.getRobot().getLocation(), marsArchitect.getCellWidth(),
                             marsArchitect.getCellWidth());
        rover.getLidar().setWallBuilder(new WallBuilder(rover.getMarsConfig()));
        rover.getLidar().scanArea();

        /* Do not render animation in case of sensor endOfLife */
        if (!rover.isEquipmentEOL()) {
            marsArchitect.setLidarAnimationEngine(rover.getLidar());
            marsArchitect.getLidarAnimationEngine().activateLidar();
            marsArchitect.returnSurfaceToNormal();

            RoverPing.Builder lidarFeedback = RoverPing.newBuilder();
            lidarFeedback.setTimeStamp(System.currentTimeMillis());
            lidarFeedback.setMsg(rover.getLidar().getStatus());

            Location location = Location.newBuilder().setX(marsArchitect.getRobot().getLocation().x)
                    .setY(marsArchitect.getRobot().getLocation().y).build();

            RoverStatus.Builder rBuilder = RoverStatus.newBuilder();
            RoverStatus status = rBuilder.setBatteryLevel(rover.getBattery().getPrimaryPowerUnits())
                    .setSolNumber(rover.getSol()).setLocation(location).setNotes("Lidar exercised here.")
                    .setModuleMessage(lidarFeedback.build().toByteString()).setSCET(System.currentTimeMillis())
                    .setModuleReporting(ModuleDirectory.Module.SENSOR_LIDAR.getValue()).build();

            rover.getMarsArchitect().returnSurfaceToNormal();
            rover.state = rover.transmittingState;
            rover.transmitMessage(status.toByteArray());
        }

        /* Flip the flag so the sensor can perform its last operation. */
        rover.setEquipmentEOL(false);
    }

    public void performDiagnostics() {
        // TODO Auto-generated method stub

    }

    public void performRadarScan() {

    }

    @Override
    public void sleep() {

    }

    @Override
    public void wakeUp() {

    }

}
