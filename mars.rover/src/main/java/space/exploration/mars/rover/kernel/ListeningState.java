package space.exploration.mars.rover.kernel;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.InstructionPayloadOuterClass;
import space.exploration.mars.rover.InstructionPayloadOuterClass.InstructionPayload;
import space.exploration.mars.rover.InstructionPayloadOuterClass.InstructionPayload.TargetPackage;
import space.exploration.mars.rover.animation.RadioAnimationEngine;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.kernel.ModuleDirectory.Module;

public class ListeningState implements State {

    private Logger logger = LoggerFactory.getLogger(ListeningState.class);
    private Rover  rover  = null;

    public ListeningState(Rover rover) {
        this.rover = rover;
    }

    @Override
    public String getStateName() {
        return "Listening State";
    }

    public void receiveMessage(byte[] message) {
        InstructionPayload payload = null;
        System.out.println("This is the listening module");
        try {
            payload = InstructionPayload.parseFrom(message);
            System.out.println(payload);
            logger.debug(payload.toString());

            RadioAnimationEngine radioAnimationEngine = new RadioAnimationEngine(rover.getMarsConfig(), rover
                    .getMarsArchitect().getMarsSurface(), rover.getMarsArchitect().getRobot(), false);
            radioAnimationEngine.activateRadio();

            for (TargetPackage tp : payload.getTargetsList()) {
                rover.writeSystemLog(tp);
                logger.info(Long.toString(System.currentTimeMillis()) + ","
                                    + Integer.toString(tp.getRoverModule()) + "," + tp.getAction());

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

                if (tp.getRoverModule() == Module.SENSOR_LIDAR.getValue()) {
                    System.out.println("Got lidar message");
                    rover.state = rover.sensingState;
                    rover.scanSurroundings();
                } else if (tp.getRoverModule() == Module.PROPULSION.getValue()) {
                    System.out.println("Rover " + Rover.ROVER_NAME + " is on the move!");
                    rover.state = rover.movingState;
                    rover.move(payload);
                } else if (tp.getRoverModule() == Module.SCIENCE.getValue()) {
                    System.out.println("Rover " + Rover.ROVER_NAME + " is on a scientific mission!");
                    rover.state = rover.exploringState;
                    rover.exploreArea();
                } else if (tp.getRoverModule() == Module.CAMERA_SENSOR.getValue()) {
                    System.out.println("Rover " + Rover.ROVER_NAME + " is going to try to take pictures!");
                    rover.state = rover.photoGraphingState;
                    rover.activateCamera();
                } else if (tp.getRoverModule() == Module.RADAR.getValue()) {
                    System.out.println("Rover " + Rover.ROVER_NAME + " will do a radarScan!");
                    rover.state = rover.radarScanningState;
                    rover.performRadarScan();
                }
            }
        } catch (InvalidProtocolBufferException e) {
            logger.error("InvalidProtocolBufferException", e);
        }
    }

    public void transmitMessage(byte[] message) {
        logger.error("Can not transmit message while in the listening state");
    }

    public void exploreArea() {
        logger.error("Can not explore area while in the listening state");
    }

    public void activateCamera() {
        logger.error("Can not perform experiments while in the listening state");
    }

    public void move(InstructionPayloadOuterClass.InstructionPayload payload) {
        logger.error("Can not move while in the listening state");
    }

    public void hibernate() {
    }

    public void rechargeBattery() {
    }

    public void scanSurroundings() {
    }

    public void performDiagnostics() {
    }

    public void performRadarScan() {

    }

}
