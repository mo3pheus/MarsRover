package space.exploration.mars.rover.kernel;

import com.google.protobuf.InvalidProtocolBufferException;
import communications.protocol.ModuleDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.InstructionPayloadOuterClass;
import space.exploration.communications.protocol.InstructionPayloadOuterClass.InstructionPayload;
import space.exploration.communications.protocol.InstructionPayloadOuterClass.InstructionPayload.TargetPackage;
import space.exploration.communications.protocol.robot.RobotPositionsOuterClass;
import space.exploration.communications.protocol.service.WeatherQueryOuterClass;
import space.exploration.mars.rover.animation.RadioAnimationEngine;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.propulsion.PropulsionUnit;

import java.awt.*;
import java.util.Properties;

public class ListeningState implements State {

    private Logger logger         = LoggerFactory.getLogger(ListeningState.class);
    private Rover  rover          = null;
    private int    powerRequested = 0;

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
        InstructionPayload payload = null;
        rover.setTimeMessageReceived(System.currentTimeMillis());
        try {
            payload = InstructionPayload.parseFrom(message);
            logger.info(payload.toString());

            RadioAnimationEngine radioAnimationEngine = new RadioAnimationEngine(rover.getMarsConfig(), rover
                    .getMarsArchitect().getMarsSurface(), rover.getMarsArchitect().getRobot(), false);
            radioAnimationEngine.activateRadio();

            for (InstructionPayload.TargetPackage tp : payload.getTargetsList()) {
                rover.writeSystemLog(tp, rover.getInstructionQueue().size());
                logger.info(Long.toString(System.currentTimeMillis()) + ","
                                    + Integer.toString(tp.getRoverModule()) + "," + tp.getAction()
                                    + " Current instructionQueue length = " + rover.getInstructionQueue().size());

                if (!requestPower(tp)) {
                    logger.error("Going into hibernation from Listening state.");

                    rover.state = rover.hibernatingState;
                    rover.setInRechargingModeTime(System.currentTimeMillis());
                    rover.getMarsArchitect().getRobot().setColor(EnvironmentUtils.findColor("robotHibernate"));

                    rover.getMarsArchitect().getMarsSurface().repaint();
                    rover.getInstructionQueue().add(payload.toByteArray());
                } else {
                    rover.getBattery().setPrimaryPowerUnits(rover.getBattery().getPrimaryPowerUnits() - powerRequested);

                    Properties marsConfig = rover.getMarsConfig();
                    Color robotColor = EnvironmentUtils.findColor(marsConfig.getProperty(EnvironmentUtils
                                                                                                 .ROBOT_COLOR));
                    rover.getMarsArchitect().getRobot().setColor(robotColor);
                    rover.getMarsArchitect().getMarsSurface().repaint();

                    if (tp.getRoverModule() == ModuleDirectory.Module.SENSOR_LIDAR.getValue()) {
                        logger.info("Got lidar message");
                        rover.state = rover.sensingState;
                        rover.scanSurroundings();
                    } else if (tp.getRoverModule() == ModuleDirectory.Module.PROPULSION.getValue()) {
                        logger.info("Rover " + Rover.ROVER_NAME + " is on the move!");
                        rover.state = rover.movingState;
                        rover.move(tp);
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
                        rover.senseWeather(null);
                    } else if (tp.getRoverModule() == ModuleDirectory.Module.SPACECRAFT_CLOCK.getValue()) {
                        logger.info("Rover will get detailed spacecraftClock information. " +
                                            "Houston, this is CuriosityActual.");
                        rover.state = rover.sclkBeepingState;
                        rover.getSclkInformation();
                    }
                }
            }
        } catch (InvalidProtocolBufferException e) {
            logger.error("InvalidProtocolBufferException", e);
            rover.writeErrorLog("InvalidProtocolBufferException", e);
        }
    }

    public boolean requestPower(TargetPackage targetPackage) throws InvalidProtocolBufferException {
        final boolean critical = false;
        if (targetPackage.getRoverModule() != ModuleDirectory.Module.PROPULSION.getValue()) {
            powerRequested = targetPackage.getEstimatedPowerUsage();
            return rover.getBattery().requestPower(powerRequested, critical);
        } else {
            java.awt.Point robotPosition = rover.getMarsArchitect().getRobot().getLocation();
            RobotPositionsOuterClass.RobotPositions positions = RobotPositionsOuterClass.RobotPositions.parseFrom
                    (targetPackage.getAuxiliaryData().toByteArray());
            RobotPositionsOuterClass.RobotPositions.Point destination = positions.getPositions(0);
            PropulsionUnit powerTran = new PropulsionUnit(rover, robotPosition, new java.awt.Point(destination.getX(),
                                                                                                   destination.getY()));
            powerRequested = powerTran.getPowerConsumptionPerUnit() * powerTran.getTrajectory().size();
            return rover.getBattery().requestPower((powerRequested), critical);
        }
    }

    public void transmitMessage(byte[] message) {
        logger.error("Can not transmit message while in the listening state");
    }

    public void exploreArea() {
        logger.error("Can not explore area while in the listening state");
    }

    public void move(InstructionPayloadOuterClass.InstructionPayload.TargetPackage targetPackage) {
        logger.debug("Can not move in " + getStateName() );
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
