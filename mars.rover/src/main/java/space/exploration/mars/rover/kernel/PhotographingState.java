package space.exploration.mars.rover.kernel;

import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.InstructionPayloadOuterClass;
import space.exploration.mars.rover.animation.CameraAnimationEngine;
import space.exploration.mars.rover.communication.RoverStatusOuterClass;
import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.robot.RobotPositionsOuterClass;

import java.util.concurrent.Semaphore;

/**
 * Created by sanketkorgaonkar on 5/9/17.
 */
public class PhotographingState implements State {
    private final Semaphore accessLock = new Semaphore(1, true);
    private       Logger    logger     = LoggerFactory.getLogger(PhotographingState.class);
    private       Rover     rover      = null;

    public PhotographingState(Rover rover) {
        this.rover = rover;
    }

    public void receiveMessage(byte[] message) {
        try {
            accessLock.acquire();
            logger.error("Photographing state received message. Saving to instruction queue");
            rover.getInstructionQueue().add(message);
            accessLock.release();
        } catch (InterruptedException ie) {
            logger.error("Photographing State's accessLock interrupted.", ie);
        }
    }

    @Override
    public String getStateName() {
        return "Photographing State";
    }

    public void transmitMessage(byte[] message) {

    }

    public void exploreArea() {

    }

    public void activateCamera() {
        try {
            accessLock.acquire();
            MarsArchitect marsArchitect = rover.getMarsArchitect();
            byte[]        cameraShot    = rover.getCamera().takePhoto(marsArchitect.getRobot().getLocation());

            /* Do not render animation in case of sensor endOfLife */
            if (!rover.isEquipmentEOL()) {
                CameraAnimationEngine cameraAnimationEngine = marsArchitect.getCameraAnimationEngine(marsArchitect
                                                                                                             .getRobot()
                                                                                                             .getLocation

                                                                                                                     ());
                cameraAnimationEngine.setMarsSurface(marsArchitect.getMarsSurface());
                cameraAnimationEngine.setRobot(marsArchitect.getRobot());
                cameraAnimationEngine.clickCamera();

                RoverStatusOuterClass.RoverStatus.Location location = RoverStatusOuterClass.RoverStatus.Location
                        .newBuilder()
                        .setX(marsArchitect.getRobot().getLocation().x)
                        .setY(marsArchitect.getRobot().getLocation().y).build();

                RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverStatusOuterClass.RoverStatus.newBuilder();

                RoverStatusOuterClass.RoverStatus status = null;
                if (cameraShot != null) {
                    System.out.println("Camera shot was not null");
                    status = rBuilder.setBatteryLevel(rover.getBattery()
                                                              .getPrimaryPowerUnits())
                            .setSolNumber(rover.getSol()).setLocation(location).setNotes("Camera used here")
                            .setModuleMessage(ByteString.copyFrom(cameraShot)).setSCET(System
                                                                                               .currentTimeMillis())
                            .setModuleReporting(ModuleDirectory.Module.CAMERA_SENSOR.getValue()).build();
                } else {
                    System.out.println("Camera shot was null");
                    status = rBuilder.setBatteryLevel(rover.getBattery()
                                                              .getPrimaryPowerUnits())
                            .setSolNumber(rover.getSol()).setLocation(location).setNotes("Camera wasn't able to take " +
                                                                                                 "a " +
                                                                                                 "shot. " +
                                                                                                 "Sorry earth!")
                            .setSCET(System
                                             .currentTimeMillis())
                            .setModuleReporting(ModuleDirectory.Module.CAMERA_SENSOR.getValue()).build();
                }

                logger.info(status.toString());

                rover.getMarsArchitect().returnSurfaceToNormal();
                rover.state = rover.transmittingState;
                rover.transmitMessage(status.toByteArray());
            }

            /* Flip the flag so the sensor can perform its last operation */
            rover.setEquipmentEOL(false);
            accessLock.release();
        } catch (InterruptedException ie) {
            logger.error("Photographing State's accessLock interrupted", ie);
        }
    }

    public void move(InstructionPayloadOuterClass.InstructionPayload payload) {
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
