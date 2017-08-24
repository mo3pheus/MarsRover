/**
 *
 */
package space.exploration.mars.rover.kernel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.InstructionPayloadOuterClass;
import space.exploration.mars.rover.animation.RadioAnimationEngine;
import space.exploration.mars.rover.robot.RobotPositionsOuterClass.RobotPositions;

import java.util.concurrent.Semaphore;

/**
 * @author sanketkorgaonkar
 */
public class TransmittingState implements State {
    private final Semaphore accessLock = new Semaphore(1, true);
    private       Rover     rover      = null;
    private       Logger    logger     = LoggerFactory.getLogger(TransmittingState.class);

    public TransmittingState(Rover rover) {
        this.rover = rover;
    }

    public void receiveMessage(byte[] message) {
        try {
            accessLock.acquire();
            rover.getInstructionQueue().add(message);
            accessLock.release();
        } catch (InterruptedException ie) {
            logger.error("Transmitting Module's accessLock interrupted.", ie);
        }
    }

    public void transmitMessage(byte[] message) {
        try {
            accessLock.acquire();
            RadioAnimationEngine radioAnimationEngine = new RadioAnimationEngine(rover.getMarsConfig(), rover
                    .getMarsArchitect().getMarsSurface(), rover.getMarsArchitect().getRobot(), true);
            radioAnimationEngine.activateRadio();
            rover.getRadio().sendMessage(message);
            rover.getMarsArchitect().returnSurfaceToNormal();
            rover.state = rover.listeningState;
            accessLock.release();
        } catch (InterruptedException ie) {
            logger.error("Transmitting Module's accessLock interrupted.", ie);
        }
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

    public void rechargeBattery() {
        // TODO Auto-generated method stub

    }

    public void scanSurroundings() {
        // TODO Auto-generated method stub

    }

    public void performDiagnostics() {
        // TODO Auto-generated method stub

    }

    public void performRadarScan() {

    }

}
