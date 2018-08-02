package space.exploration.mars.rover.kernel;

import com.google.protobuf.InvalidProtocolBufferException;
import com.yammer.metrics.core.Meter;
import communications.protocol.ModuleDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.InstructionPayloadOuterClass;
import space.exploration.communications.protocol.InstructionPayloadOuterClass.InstructionPayload;
import space.exploration.communications.protocol.InstructionPayloadOuterClass.InstructionPayload.TargetPackage;
import space.exploration.communications.protocol.robot.RobotPositionsOuterClass;
import space.exploration.communications.protocol.service.SamQueryOuterClass;
import space.exploration.communications.protocol.service.WeatherQueryOuterClass;
import space.exploration.communications.protocol.softwareUpdate.SwUpdatePackageOuterClass;
import space.exploration.kernel.diagnostics.LogRequest;
import space.exploration.mars.rover.animation.RadioAnimationEngine;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.kernel.config.RoverConfig;
import space.exploration.mars.rover.propulsion.AStarPropulsionUnit;
import space.exploration.mars.rover.propulsion.LearningPropulsionUnit;
import space.exploration.mars.rover.propulsion.PropulsionUnit;
import space.exploration.mars.rover.utils.RoverUtil;

import java.awt.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static communications.protocol.ModuleDirectory.*;

public class ListeningState implements State {
    private Meter  requests       = null;
    private Logger logger         = LoggerFactory.getLogger(ListeningState.class);
    private Rover  rover          = null;
    private int    powerRequested = 0;

    public ListeningState(Rover rover) {
        this.rover = rover;
        requests = this.rover.getMetrics().newMeter(ListeningState.class, getStateName(), "requests", TimeUnit.HOURS);
    }

    @Override
    public void activateCameraById(String camId) {

    }

    @Override
    public void sampleAnalysis(SamQueryOuterClass.SamQuery samQuery) {
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
    public void updateSoftware(SwUpdatePackageOuterClass.SwUpdatePackage swUpdatePackage) {
    }

    @Override
    public void requestLogs(LogRequest.LogRequestPacket logRequestPacket) {
    }

    @Override
    public String getStateName() {
        return "Listening State";
    }

    public void receiveMessage(byte[] message) {
        requests.mark();
        InstructionPayload payload = null;
        rover.setTimeMessageReceived(System.currentTimeMillis());
        rover.reflectRoverState();
        try {
            payload = InstructionPayload.parseFrom(message);
            logger.info(payload.toString());

            RadioAnimationEngine radioAnimationEngine = new RadioAnimationEngine(rover.getRoverConfig().getMarsConfig
                    (), rover.getMarsArchitect().getMarsSurface(), rover.getMarsArchitect().getRobot(), false);
            radioAnimationEngine.activateRadio();

            for (InstructionPayload.TargetPackage tp : payload.getTargetsList()) {
                RoverUtil.writeSystemLog(rover, tp, rover.getInstructionQueue().size());
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

                    Properties marsConfig = rover.getRoverConfig().getMarsConfig();
                    Color robotColor = EnvironmentUtils.findColor(marsConfig.getProperty(EnvironmentUtils
                                                                                                 .ROBOT_COLOR));
                    rover.getMarsArchitect().getRobot().setColor(robotColor);
                    rover.getMarsArchitect().getMarsSurface().repaint();

                    if (tp.getRoverModule() == ModuleDirectory.Module.SENSOR_LIDAR.getValue()) {
                        logger.info("Got lidar message");
                        rover.state = rover.sensingState;
                        rover.scanSurroundings();
                    } else if (tp.getRoverModule() == ModuleDirectory.Module.PROPULSION.getValue()) {
                        logger.info("Rover " + RoverConfig.ROVER_NAME + " is on the move!");
                        rover.state = rover.movingState;
                        rover.move(tp);
                    } else if (tp.getRoverModule() == ModuleDirectory.Module.SCIENCE.getValue()) {
                        logger.info("Rover " + RoverConfig.ROVER_NAME + " is on a scientific mission!");
                        rover.state = rover.exploringState;
                        rover.exploreArea();
                    } else if (tp.getRoverModule() == ModuleDirectory.Module.CAMERA_SENSOR.getValue()) {
                        logger.info("Rover " + RoverConfig.ROVER_NAME + " is going to try to take pictures!");
                        rover.state = rover.photoGraphingState;
                        rover.activateCameraById(tp.getRoverSubModule());
                    } else if (tp.getRoverModule() == ModuleDirectory.Module.RADAR.getValue()) {
                        logger.info("Rover " + RoverConfig.ROVER_NAME + " will do a radarScan!");
                        rover.state = rover.radarScanningState;
                        rover.performRadarScan();
                    } else if (tp.getRoverModule() == ModuleDirectory.Module.WEATHER_SENSOR.getValue()) {
                        logger.info("Rover will try to get weather measurements - actual Curiosity Data");
                        rover.state = rover.weatherSensingState;
                        rover.senseWeather(null);
                    } else if (tp.getRoverModule() == Module.SAM_SPECTROMETER.getValue()) {
                        logger.info("Rover will attempt sampleAnalysis on Mars for sol = " + rover.getSpacecraftClock
                                ().getSol());
                        try {
                            SamQueryOuterClass.SamQuery samQuery = SamQueryOuterClass.SamQuery.parseFrom(tp.getAuxiliaryData());
                            rover.state = rover.sampleAnalysisState;
                            rover.sampleAnalysis(samQuery);
                        } catch (InvalidProtocolBufferException ipb) {
                            logger.error("Error while parsing samQuery.", ipb);
                            rover.state = rover.listeningState;
                        }
                    } else if (tp.getRoverModule() == ModuleDirectory.Module.KERNEL.getValue()) {
                        rover.state = rover.maintenanceState;
                        switch (tp.getAction()) {
                            case GRACEFUL_SHUTDOWN: {
                                rover.setGracefulShutdown(true);
                                rover.saveOffSensorLifespans();
                                rover.gracefulShutdown();
                            }
                            break;
                            case SOFTWARE_UPDATE: {
                                rover.saveOffSensorLifespans();
                                SwUpdatePackageOuterClass.SwUpdatePackage swUpdatePackage = SwUpdatePackageOuterClass
                                        .SwUpdatePackage.parseFrom(tp.getAuxiliaryData());
                                rover.updateSoftware(swUpdatePackage);
                            }
                            break;
                            case REQUEST_LOGS: {
                                LogRequest.LogRequestPacket logRequestPacket = LogRequest.LogRequestPacket.parseFrom
                                        (tp.getAuxiliaryData());
                                rover.requestLogs(logRequestPacket);
                            }
                            break;
                            default: {
                                logger.warn("Unknown action specified for Kernel module.");
                            }
                        }
                    } else if (tp.getRoverModule() == ModuleDirectory.Module.DAN_SPECTROMETER.getValue()) {
                        logger.info(" Rover " + RoverConfig.ROVER_NAME + " will do a Dynamic Albedo of Neutrons Scan");
                        rover.state = rover.danSensingState;
                        rover.shootNeutrons();
                    } else if (tp.getRoverModule() == ModuleDirectory.Module.SPACECRAFT_CLOCK.getValue()) {
                        logger.info("Rover will get detailed spacecraftClock information. " +
                                            "Houston, this is CuriosityActual.");
                        rover.state = rover.sclkBeepingState;

                        switch (tp.getAction()) {
                            case SCLK_COMMAND: {
                                rover.getSclkInformation();
                            }
                            break;
                            case SCLK_SYNC: {
                                rover.synchronizeClocks(tp.getUtcTime());
                            }
                            break;
                            default: {
                                logger.error("Unknown action specified for Sclk articulation.");
                            }
                        }
                    }
                }
            }
        } catch (InvalidProtocolBufferException e) {
            logger.error("InvalidProtocolBufferException", e);
            RoverUtil.writeErrorLog(rover, "InvalidProtocolBufferException", e);
        }
    }

    private boolean requestPower(TargetPackage targetPackage) throws InvalidProtocolBufferException {
        final boolean critical = false;
        if (targetPackage.getRoverModule() != ModuleDirectory.Module.PROPULSION.getValue()) {
            powerRequested = targetPackage.getEstimatedPowerUsage();
            return rover.getBattery().requestPower(powerRequested, critical);
        } else {
            java.awt.Point robotPosition = rover.getMarsArchitect().getRobot().getLocation();
            RobotPositionsOuterClass.RobotPositions positions = RobotPositionsOuterClass.RobotPositions.parseFrom
                    (targetPackage.getAuxiliaryData().toByteArray());
            RobotPositionsOuterClass.RobotPositions.Point destination = positions.getPositions(0);

            String propulsionChoice = rover.getRoverConfig().getMarsConfig().getProperty(RoverConfig.PROPULSION_CHOICE);
            AStarPropulsionUnit aStarPropulsionUnit = new AStarPropulsionUnit(rover, robotPosition, new java.awt.Point
                    (destination.getX(),
                     destination.getY()));
            LearningPropulsionUnit learningPropulsionUnit = new LearningPropulsionUnit(rover, robotPosition, new java
                    .awt
                    .Point(destination.getX(),
                           destination.getY()));

            PropulsionUnit powerTran = (propulsionChoice.equals("rl")) ? learningPropulsionUnit : aStarPropulsionUnit;
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
        logger.debug("Can not move in " + getStateName());
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
    public void gracefulShutdown() {

    }

    @Override
    public void sleep() {

    }

    @Override
    public void wakeUp() {

    }

    @Override
    public void shootNeutrons() {
    }

    @Override
    public void getSclkInformation() {
        logger.error("Can not get sclkInformation while in listeningState");
    }
}
