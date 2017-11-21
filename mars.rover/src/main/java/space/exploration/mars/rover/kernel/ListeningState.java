package space.exploration.mars.rover.kernel;

import com.google.protobuf.InvalidProtocolBufferException;
import communications.protocol.ModuleDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.InstructionPayloadOuterClass;
import space.exploration.communications.protocol.service.WeatherQueryOuterClass;
import space.exploration.mars.rover.animation.RadioAnimationEngine;
import space.exploration.mars.rover.environment.EnvironmentUtils;

public class ListeningState implements State {

    private Logger logger = LoggerFactory.getLogger(ListeningState.class);
    private Rover  rover  = null;

    public ListeningState(Rover rover) {
        this.rover = rover;
    }

    @Override
    public void activateCameraById(String camId) {

    }

    @Override
    public String getStateName() {
        return "Listening State";
    }

    public void receiveMessage(byte[] message) {
        InstructionPayloadOuterClass.InstructionPayload payload = null;
        rover.setTimeMessageReceived(System.currentTimeMillis());
        System.out.println("This is the listening module");
        try {
            payload = InstructionPayloadOuterClass.InstructionPayload.parseFrom(message);
            System.out.println(payload);
            logger.debug(payload.toString());

            RadioAnimationEngine radioAnimationEngine = new RadioAnimationEngine(rover.getMarsConfig(), rover
                    .getMarsArchitect().getMarsSurface(), rover.getMarsArchitect().getRobot(), false);
            radioAnimationEngine.activateRadio();

            for (InstructionPayloadOuterClass.InstructionPayload.TargetPackage tp : payload.getTargetsList()) {
                rover.writeSystemLog(tp, rover.getInstructionQueue().size());
                logger.info(Long.toString(System.currentTimeMillis()) + ","
                                    + Integer.toString(tp.getRoverModule()) + "," + tp.getAction()
                                    + " Current instructionQueue length = " + rover.getInstructionQueue().size());

                if (!rover.getBattery().requestPower(tp.getEstimatedPowerUsage(), false)) {
                    logger.error("Going into hibernation from Listening state.");

                    rover.state = rover.hibernatingState;
                    rover.setInRechargingModeTime(System.currentTimeMillis());
                    rover.getMarsArchitect().getRobot().setColor(EnvironmentUtils.findColor("robotHibernate"));

                    rover.getMarsArchitect().getMarsSurface().repaint();
                    rover.getInstructionQueue().add(payload.toByteArray());
                    return;
                }

                rover.getMarsArchitect().getRobot().setColor(EnvironmentUtils.findColor(rover.getMarsConfig()
                                                                                                .getProperty
                                                                                                        (EnvironmentUtils
                                                                                                                 .ROBOT_COLOR)));
                rover.getMarsArchitect().getMarsSurface().repaint();
                rover.getBattery().setPrimaryPowerUnits(rover.getBattery().getPrimaryPowerUnits() - tp
                        .getEstimatedPowerUsage());

                if (tp.getRoverModule() == ModuleDirectory.Module.SENSOR_LIDAR.getValue()) {
                    logger.info("Got lidar message");
                    rover.state = rover.sensingState;
                    rover.scanSurroundings();
                } else if (tp.getRoverModule() == ModuleDirectory.Module.PROPULSION.getValue()) {
                    logger.info("Rover " + Rover.ROVER_NAME + " is on the move!");
                    rover.state = rover.movingState;
                    rover.move(payload);
                } else if (tp.getRoverModule() == ModuleDirectory.Module.SCIENCE.getValue()) {
                    logger.info("Rover " + Rover.ROVER_NAME + " is on a scientific mission!");
                    rover.state = rover.exploringState;
                    rover.exploreArea();
                } else if (tp.getRoverModule() == ModuleDirectory.Module.CAMERA_SENSOR.getValue()) {
                    logger.info("Rover " + Rover.ROVER_NAME + " is going to try to take pictures!");
                    rover.state = rover.photoGraphingState;
                    rover.activateCameraById(tp.getRoverSubModule());
                } else if (tp.getRoverModule() == ModuleDirectory.Module.RADAR.getValue()) {
                    logger.info("Rover " + Rover.ROVER_NAME + " will do a radarScan!");
                    rover.state = rover.radarScanningState;
                    rover.performRadarScan();
                } else if (tp.getRoverModule() == ModuleDirectory.Module.WEATHER_SENSOR.getValue()) {
                    logger.info("Rover will try to get weather measurements - actual Curiosity Data");
                    rover.state = rover.weatherSensingState;
                    rover.senseWeather(WeatherQueryOuterClass.WeatherQuery.parseFrom(tp.getAuxiliaryData()));
                } else if (tp.getRoverModule() == ModuleDirectory.Module.SPACECRAFT_CLOCK.getValue()) {
                    logger.info("Rover will get detailed spacecraftClock information. " +
                                        "Houston, this is CuriosityActual.");
                    rover.state = rover.sclkBeepingState;
                    rover.getSclkInformation();
                }
            }
        } catch (InvalidProtocolBufferException e) {
            logger.error("InvalidProtocolBufferException", e);
            rover.writeErrorLog("InvalidProtocolBufferException", e);
        }
    }

    public void transmitMessage(byte[] message) {
        logger.error("Can not transmit message while in the listening state");
    }

    public void exploreArea() {
        logger.error("Can not explore area while in the listening state");
    }

    public void move(InstructionPayloadOuterClass.InstructionPayload payload) {
        logger.error("Can not move while in the listening state");
    }

    public void hibernate() {
    }

    public void senseWeather(WeatherQueryOuterClass.WeatherQuery weatherQuery) {
    }

    public void scanSurroundings() {
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
        logger.error("Can not get sclkInformation while in listeningState");
    }
}
