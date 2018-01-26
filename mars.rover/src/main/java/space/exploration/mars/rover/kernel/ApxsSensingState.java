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
import space.exploration.communications.protocol.service.WeatherQueryOuterClass;
import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.sensors.apxs.ApxsData;

import java.util.concurrent.TimeUnit;

/**
 * @author sanketkorgaonkar
 */
public class ApxsSensingState implements State {
    private Meter  requests = null;
    private Rover  rover    = null;
    private Logger logger   = LoggerFactory.getLogger(ApxsSensingState.class);

    public ApxsSensingState(Rover rover) {
        this.rover = rover;
        requests = this.rover.getMetrics().newMeter(ApxsSensingState.class, "ApxsSpectrometer", "requests", TimeUnit
                .HOURS);
    }

    public void receiveMessage(byte[] message) {
        rover.reflectRoverState();
        rover.getInstructionQueue().add(message);
        try {
            rover.writeSystemLog(InstructionPayloadOuterClass.InstructionPayload.parseFrom(message), rover
                    .getInstructionQueue().size());
        } catch (InvalidProtocolBufferException ipe) {
            rover.writeErrorLog("Invalid Protocol Buffer Exception", ipe);
        }
    }

    public void shootNeutrons() {
    }

    public void transmitMessage(byte[] message) {
    }

    public void exploreArea() {
        requests.mark();
        rover.reflectRoverState();

        MarsArchitect marsArchitect = rover.getMarsArchitect();
        Cell          robot         = marsArchitect.getRobot();

        RoverStatusOuterClass.RoverStatus.Location.Builder lBuilder = RoverStatusOuterClass.RoverStatus.Location
                .newBuilder().setX(robot.getLocation().x).setY(robot.getLocation
                        ().y);

        ApxsData.ApxsDataPacket           apxsDataPacket = rover.getApxsSpectrometer().getApxsDataPacket();
        RoverStatusOuterClass.RoverStatus status         = null;

        if (apxsDataPacket == null) {
            String errorString = "ApxsData unavailable for sol = " + rover.getSpacecraftClock().getSol();
            logger.error(errorString);
            RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverStatusOuterClass.RoverStatus.newBuilder();
            status = rBuilder.setBatteryLevel(rover.getBattery()
                                                      .getPrimaryPowerUnits())
                    .setSolNumber(rover.getSpacecraftClock().getSol())
                    .setLocation(lBuilder.build()).setNotes("Spectroscope engaged!")
                    .setSCET(System.currentTimeMillis()).setModuleReporting(ModuleDirectory.Module.SCIENCE
                                                                                    .getValue())
                    .setNotes(errorString)
                    .build();
        } else {
            marsArchitect.getSpectrometerAnimationEngine().renderAnimation();
            marsArchitect.returnSurfaceToNormal();

            RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverStatusOuterClass.RoverStatus.newBuilder();
            status = rBuilder.setBatteryLevel(rover.getBattery()
                                                      .getPrimaryPowerUnits())
                    .setSolNumber(rover.getSpacecraftClock().getSol())
                    .setLocation(lBuilder.build()).setNotes("Spectroscope engaged!")
                    .setModuleMessage(apxsDataPacket.toByteString())
                    .setSCET(System.currentTimeMillis()).setModuleReporting(ModuleDirectory.Module.SCIENCE
                                                                                    .getValue())
                    .build();
        }

        rover.state = rover.transmittingState;
        rover.transmitMessage(status.toByteArray());
    }

    public void move(InstructionPayloadOuterClass.InstructionPayload.TargetPackage targetPackage) {
        logger.debug("Can not move in " + getStateName());
    }

    public void hibernate() {
    }

    public void senseWeather(WeatherQueryOuterClass.WeatherQuery weatherQuery) {
    }

    public void scanSurroundings() {
    }

    @Override
    public void activateCameraById(String camId) {

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
        logger.error("Can not get sclkInformation while in exploringState");
    }

    @Override
    public String getStateName() {
        return "ApxsSensingState";
    }

    @Override
    public void synchronizeClocks(String utcTime) {
        logger.debug("Can not sync clocks in " + getStateName());
    }

    @Override
    public void gracefulShutdown() {
        logger.error(" Can not perform gracefulShutdown while in " + getStateName());
    }

    @Override
    public Meter getRequests() {
        return requests;
    }
}
