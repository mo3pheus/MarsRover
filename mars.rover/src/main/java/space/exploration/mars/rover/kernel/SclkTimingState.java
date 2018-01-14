package space.exploration.mars.rover.kernel;

import com.google.protobuf.InvalidProtocolBufferException;
import com.yammer.metrics.core.Meter;
import communications.protocol.ModuleDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.InstructionPayloadOuterClass;
import space.exploration.communications.protocol.communication.RoverStatusOuterClass;
import space.exploration.communications.protocol.service.WeatherQueryOuterClass;
import space.exploration.communications.protocol.spacecraftClock.SpacecraftClock.SclkPacket;
import space.exploration.mars.rover.diagnostics.Pacemaker;
import space.exploration.mars.rover.diagnostics.RoverGarbageCollector;
import space.exploration.mars.rover.utils.RoverUtil;

import java.util.concurrent.TimeUnit;

public class SclkTimingState implements State {
    private Meter  requests = null;
    private Logger logger   = LoggerFactory.getLogger(SclkTimingState.class);
    private Rover  rover    = null;

    public SclkTimingState(Rover rover) {
        this.rover = rover;
        requests = this.rover.getMetrics().newMeter(SclkTimingState.class, getStateName(), "requests", TimeUnit
                .HOURS);
    }

    @Override
    public void receiveMessage(byte[] message) {
        logger.info("Saving the message to the instruction queue. Current instructionQueue length = " + rover
                .getInstructionQueue().size());
        rover.reflectRoverState();
        rover.getInstructionQueue().add(message);
        try {
            rover.writeSystemLog(InstructionPayloadOuterClass.InstructionPayload.parseFrom(message), rover
                    .getInstructionQueue().size());
        } catch (InvalidProtocolBufferException ipe) {
            rover.writeErrorLog("Invalid Protocol Buffer Exception", ipe);
        }
    }

    @Override
    public void shootNeutrons() {
    }

    @Override
    public Meter getRequests() {
        return requests;
    }

    @Override
    public void synchronizeClocks(String utcTime) {
        try {
            logger.info("Synchronizing sclk and positionSensor to " + utcTime);

            logger.info("Houston, expect a diagnostics pause. Shutting down paceMaker");
            rover.reflectRoverState();
            rover.getPacemaker().interrupt();

            logger.info("Houston, be advised - garbageCollection is paused. Please wait before sending new signals.");
            rover.getGarbageCollector().interrupt();

            rover.getSpacecraftClock().resetSpacecraftClock(utcTime);
            rover.getSpacecraftClock().start();

            rover.getPositionSensor().resetPositionSensor(utcTime);
            rover.getPositionSensor().start();

            logger.info("Restarting diagnostics heartbeat");
            rover.setPaceMaker(new Pacemaker(rover));
            rover.getPacemaker().pulse();

            logger.info("Restarting garbageCollection");
            rover.setGarbageCollector(new RoverGarbageCollector(rover));
            rover.getGarbageCollector().start();
        } catch (Exception e) {
            logger.error("Clock synchronization was unsuccessful. Attempting to restore the rover to previous state",
                         e);
            rover.getPacemaker().pulse();
            rover.getGarbageCollector().start();
            rover.getSpacecraftClock().start();
            rover.getPositionSensor().start();
            rover.setState(rover.getListeningState());
        } finally {
            getSclkInformation();
        }
    }

    @Override
    public void gracefulShutdown() {
        logger.error(" Can not perform gracefulShutdown while in " + getStateName());
    }

    @Override
    public void transmitMessage(byte[] message) {
        logger.error("Can not transmit from SclkTimingState");
    }

    @Override
    public void exploreArea() {
        logger.error("Can not exploreArea from SclkTimingState");
    }

    @Override
    public void move(InstructionPayloadOuterClass.InstructionPayload.TargetPackage targetPackage) {
        logger.debug("Can not move in " + getStateName());
    }

    @Override
    public void hibernate() {
        logger.error("Can not hibernate when in SclkTimingState");
    }

    @Override
    public void senseWeather(WeatherQueryOuterClass.WeatherQuery weatherQuery) {
        logger.error("Can not senseWeather when in SclkTimingState");
    }

    @Override
    public void scanSurroundings() {
        logger.error("Can not scanSurroundings when in SclkTimingState");
    }

    @Override
    public void activateCameraById(String camId) {
        logger.error("Can not activateCamera when in SclkTimingState");
    }

    @Override
    public void performRadarScan() {
        logger.error("Can not performRadarScan when in SclkTimingState");
    }

    @Override
    public void sleep() {
        logger.error("Can not sleep when in SclkTimingState");
    }

    @Override
    public void wakeUp() {
        logger.error("Can not wakeUp when in SclkTimingState");
    }

    @Override
    public void getSclkInformation() {
        rover.reflectRoverState();
        RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverStatusOuterClass.RoverStatus.newBuilder();
        rBuilder.setModuleReporting(ModuleDirectory.Module.SPACECRAFT_CLOCK.getValue());
        rBuilder.setModuleName(ModuleDirectory.Module.SPACECRAFT_CLOCK.getName());
        rBuilder.setBatteryLevel(rover.getBattery().getPrimaryPowerUnits());
        rBuilder.setLocation(RoverUtil.getLocation(rover.getMarsArchitect().getRobot().getLocation()));

        SclkPacket.Builder sBuilder = SclkPacket.newBuilder();
        sBuilder.setMissionDurationMS(rover.getSpacecraftClock().getMissionDuration());
        sBuilder.setSclkValue(rover.getSpacecraftClock().getSclkTime());
        sBuilder.setStartTime(rover.getSpacecraftClock().getSclkStartTime());
        sBuilder.setTimeElapsedMs(rover.getSpacecraftClock().getTimeElapsedMs());
        sBuilder.setTimeScaleFactor(rover.getSpacecraftClock().getTimeScaleFactor());
        sBuilder.setUtcTime(rover.getSpacecraftClock().getUTCTime());
        sBuilder.setApplicableTimeFrame(rover.getSpacecraftClock().getApplicableTimeFrame());
        sBuilder.setCalendarTime(rover.getSpacecraftClock().getCalendarTime());
        sBuilder.setClockFile(rover.getSpacecraftClock().getClockFilePath());
        sBuilder.setEphemerisTime(rover.getSpacecraftClock().getEphemerisTime());
        sBuilder.setSol(rover.getSpacecraftClock().getSol());

        rBuilder.setModuleMessage(sBuilder.build().toByteString());

        rover.setState(rover.getTransmittingState());
        rover.transmitMessage(rBuilder.build().toByteArray());
    }

    @Override
    public String getStateName() {
        return "SclkTiming State";
    }
}
