package space.exploration.mars.rover.kernel;

import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.animation.CameraAnimationEngine;
import space.exploration.mars.rover.communication.RoverStatusOuterClass;
import space.exploration.mars.rover.communication.SerializationUtil;
import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.robot.RobotPositionsOuterClass;
import space.exploration.mars.rover.sensor.Camera;

/**
 * Created by sanketkorgaonkar on 5/9/17.
 */
public class PhotographingState implements State {
    private Logger logger = LoggerFactory.getLogger(PhotographingState.class);
    private Rover  rover  = null;

    public PhotographingState(Rover rover) {
        this.rover = rover;
    }

    public void receiveMessage(byte[] message) {

    }

    public void transmitMessage(byte[] message) {

    }

    public void exploreArea() {

    }

    public void activateCamera() {
        MarsArchitect marsArchitect = rover.getMarsArchitect();
        CameraAnimationEngine cameraAnimationEngine = marsArchitect.getCameraAnimationEngine(marsArchitect.getRobot()
                .getLocation());
        cameraAnimationEngine.clickCamera();
        Camera.Photo cameraShot = rover.getCamera().takePhoto(marsArchitect.getRobot().getLocation());

        RoverStatusOuterClass.RoverStatus.Location location = RoverStatusOuterClass.RoverStatus.Location.newBuilder()
                .setX(marsArchitect.getRobot().getLocation().x)
                .setY(marsArchitect.getRobot().getLocation().y).build();

        RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverStatusOuterClass.RoverStatus.newBuilder();

        RoverStatusOuterClass.RoverStatus status = null;
        if (cameraShot != null) {
            status = rBuilder.setBatteryLevel(rover.getBattery()
                    .getPrimaryPowerUnits())
                    .setSolNumber(rover.getSol()).setLocation(location).setNotes("Camera used here")
                    .setModuleMessage(ByteString.copyFrom(SerializationUtil.serialize(cameraShot))).setSCET(System
                            .currentTimeMillis())
                    .setModuleReporting(ModuleDirectory.Module.CAMERA_SENSOR.getValue()).build();
        } else {
            status = rBuilder.setBatteryLevel(rover.getBattery()
                    .getPrimaryPowerUnits())
                    .setSolNumber(rover.getSol()).setLocation(location).setNotes("Camera wasn't able to take a shot. " +
                            "Sorry earth!")
                    .setSCET(System
                            .currentTimeMillis())
                    .setModuleReporting(ModuleDirectory.Module.CAMERA_SENSOR.getValue()).build();
        }


        logger.info(status.toString());

        rover.state = rover.transmittingState;
        rover.transmitMessage(status.toByteArray());
    }

    public void move(RobotPositionsOuterClass.RobotPositions positions) {

    }

    public void hibernate() {

    }

    public void rechargeBattery() {

    }

    public void scanSurroundings() {

    }

    public void performDiagnostics() {

    }
}
