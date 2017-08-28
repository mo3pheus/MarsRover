/**
 *
 */
package space.exploration.mars.rover.kernel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.InstructionPayloadOuterClass;
import space.exploration.mars.rover.robot.RobotPositionsOuterClass.RobotPositions;

import java.util.concurrent.Semaphore;

/**
 * @author sanketkorgaonkar
 */
public class HibernatingState implements State {
    private Rover     rover      = null;
    private Logger    logger     = LoggerFactory.getLogger(HibernatingState.class);
    private Semaphore accessLock = new Semaphore(1, true);

    public HibernatingState(Rover rover) {
        this.rover = rover;
    }

    public void receiveMessage(byte[] message) {
        logger.error("In hibernating state's receiveMessage(), adding message to instruction queue");
        rover.getInstructionQueue().add(message);
    }

    @Override
    public String getStateName() {
        return "Hibernating State";
    }

    public void transmitMessage(byte[] message) {
        logger.error("Can not transmit message in hibernating state.");
    }

    public void exploreArea() {
        logger.error("Can not explore area in hibernating state.");
    }

    public void activateCamera() {
        logger.error("Can not activate camera in hibernating state.");
    }

    public void move(InstructionPayloadOuterClass.InstructionPayload payload) {
        logger.error("Can not move in hibernating state.");
    }

    public void hibernate() {
        logger.error("Already in hibernating state.");
    }

    public void rechargeBattery() {
        logger.error("Should already be recharging the battery");
    }

    public void scanSurroundings() {
        logger.error("Can not scan surroundings in hibernating state.");
    }

    public void performDiagnostics() {
        logger.error("Diagnostics disabled in hibernating state.");
    }

    public void performRadarScan() {
        logger.error("Can not perform radar scan in hibernating state.");
    }
}
