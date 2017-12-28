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
import space.exploration.communications.protocol.communication.RoverPingOuterClass;
import space.exploration.communications.protocol.communication.RoverStatusOuterClass;
import space.exploration.communications.protocol.service.WeatherQueryOuterClass;
import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.environment.WallBuilder;

import java.util.concurrent.TimeUnit;

/**
 * @author sanketkorgaonkar
 */
public class SensingState implements State {
    private Meter  requests = null;
    private Logger logger   = LoggerFactory.getLogger(SensingState.class);
    private Rover  rover    = null;

    public SensingState(Rover rover) {
        this.rover = rover;
        requests = this.rover.getMetrics().newMeter(SensingState.class, getStateName(), "requests", TimeUnit.HOURS);
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
    public Meter getRequests() {
        return requests;
    }

    @Override
    public void synchronizeClocks(String utcTime) {
        logger.debug("Can not sync clocks in " + getStateName());
    }

    @Override
    public String getStateName() {
        return "Sensing State";
    }

    public void transmitMessage(byte[] message) {
    }

    public void exploreArea() {
    }

    public void move(InstructionPayloadOuterClass.InstructionPayload.TargetPackage targetPackage) {
        logger.debug("Can not move in " + getStateName());
    }

    public void hibernate() {
    }

    public void senseWeather(WeatherQueryOuterClass.WeatherQuery weatherQuery) {
    }

    @Override
    public void gracefulShutdown() {
        logger.error(" Can not perform gracefulShutdown while in " + getStateName());
    }

    @Override
    public void activateCameraById(String camId) {
    }

    public void scanSurroundings() {
        requests.mark();
        MarsArchitect marsArchitect = rover.getMarsArchitect();

        rover.configureLidar(marsArchitect.getRobot().getLocation(), marsArchitect.getCellWidth(),
                             marsArchitect.getCellWidth());
        rover.getLidar().setWallBuilder(new WallBuilder(rover.getMarsConfig()));
        rover.getLidar().scanArea();

        marsArchitect.setLidarAnimationEngine(rover.getLidar());
        marsArchitect.getLidarAnimationEngine().activateLidar();
        marsArchitect.returnSurfaceToNormal();

        RoverPingOuterClass.RoverPing.Builder lidarFeedback = RoverPingOuterClass.RoverPing.newBuilder();
        lidarFeedback.setTimeStamp(System.currentTimeMillis());
        lidarFeedback.setMsg(rover.getLidar().getStatus());

        RoverStatusOuterClass.RoverStatus.Location location = RoverStatusOuterClass.RoverStatus.Location.newBuilder()
                .setX(marsArchitect.getRobot().getLocation().x)
                .setY(marsArchitect.getRobot().getLocation().y).build();

        RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverStatusOuterClass.RoverStatus.newBuilder();
        RoverStatusOuterClass.RoverStatus status = rBuilder.setBatteryLevel(rover.getBattery().getPrimaryPowerUnits())
                .setSolNumber(rover.getSpacecraftClock().getSol()).setLocation(location).setNotes("Lidar exercised " +
                                                                                                          "here.")
                .setModuleMessage(lidarFeedback.build().toByteString()).setSCET(System.currentTimeMillis())
                .setModuleReporting(ModuleDirectory.Module.SENSOR_LIDAR.getValue()).build();

        rover.getMarsArchitect().returnSurfaceToNormal();
        rover.state = rover.transmittingState;
        rover.transmitMessage(status.toByteArray());
    }

    public void activateCameraById() {
    }

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

    }
}
