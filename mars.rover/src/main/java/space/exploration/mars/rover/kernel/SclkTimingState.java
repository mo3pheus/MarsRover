package space.exploration.mars.rover.kernel;

import communications.protocol.ModuleDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.InstructionPayloadOuterClass;
import space.exploration.communications.protocol.communication.RoverStatusOuterClass;
import space.exploration.communications.protocol.service.WeatherQueryOuterClass;
import space.exploration.communications.protocol.spacecraftClock.SpacecraftClock.SclkPacket;
import space.exploration.mars.rover.utils.RoverUtil;

public class SclkTimingState implements State {
    private Logger logger = LoggerFactory.getLogger(SclkTimingState.class);
    private Rover  rover  = null;

    public SclkTimingState(Rover rover) {
        this.rover = rover;
    }

    @Override
    public void receiveMessage(byte[] message) {
        logger.info("Saving the message to the instruction queue. Current instructionQueue length = " + rover
                .getInstructionQueue().size());
        rover.getInstructionQueue().add(message);
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
    public void move(InstructionPayloadOuterClass.InstructionPayload payload) {
        logger.error("Can not move when in SclkTimingState");
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

        rBuilder.setModuleMessage(sBuilder.build().toByteString());

        rover.setState(rover.getTransmittingState());
        rover.transmitMessage(rBuilder.build().toByteArray());
    }

    @Override
    public String getStateName() {
        return "SclkTiming State";
    }
}
