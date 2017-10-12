/**
 *
 */
package space.exploration.mars.rover.kernel;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.InstructionPayloadOuterClass;
import space.exploration.mars.rover.animation.RadioAnimationEngine;

/**
 * @author sanketkorgaonkar
 */
public class TransmittingState implements State {
    private Rover  rover  = null;
    private Logger logger = LoggerFactory.getLogger(TransmittingState.class);

    public TransmittingState(Rover rover) {
        this.rover = rover;
    }

    public void receiveMessage(byte[] message) {
        rover.getInstructionQueue().add(message);
        try {
            rover.writeSystemLog(InstructionPayloadOuterClass.InstructionPayload.parseFrom(message), rover
                    .getInstructionQueue().size());
        } catch (InvalidProtocolBufferException ipe) {
            rover.writeErrorLog("InvalidProtocolBufferException", ipe);
        }
    }

    public void transmitMessage(byte[] message) {
        RadioAnimationEngine radioAnimationEngine = new RadioAnimationEngine(rover.getMarsConfig(), rover
                .getMarsArchitect().getMarsSurface(), rover.getMarsArchitect().getRobot(), true);
        radioAnimationEngine.activateRadio();
        rover.getRadio().sendMessage(message);
        rover.getMarsArchitect().returnSurfaceToNormal();
        rover.state = rover.listeningState;
    }

    @Override
    public void activateCameraById(String camId) {

    }

    @Override
    public String getStateName() {
        return "Transmitting State";
    }

    public void exploreArea() {
        // TODO Auto-generated method stub

    }

    public void activateCamera() {
        // TODO Auto-generated method stub

    }

    public void move(InstructionPayloadOuterClass.InstructionPayload payload) {
    }

    public void hibernate() {
        // TODO Auto-generated method stub

    }

    public void senseWeather() {
        // TODO Auto-generated method stub

    }

    public void scanSurroundings() {
        // TODO Auto-generated method stub

    }

    public void activateCameraById() {
        // TODO Auto-generated method stub

    }

    public void performRadarScan() {

    }

    @Override
    public void sleep() {

    }

    @Override
    public void wakeUp() {

    }
}
