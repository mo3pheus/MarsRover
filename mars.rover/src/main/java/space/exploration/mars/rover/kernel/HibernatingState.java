/**
 *
 */
package space.exploration.mars.rover.kernel;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.InstructionPayloadOuterClass;

/**
 * @author sanketkorgaonkar
 */
public class HibernatingState implements State {
    private Rover  rover  = null;
    private Logger logger = LoggerFactory.getLogger(HibernatingState.class);

    public HibernatingState(Rover rover) {
        this.rover = rover;
    }

    public void receiveMessage(byte[] message) {
        logger.info("In hibernating state's receiveMessage(), adding message to instruction queue");
        rover.getInstructionQueue().add(message);
        try {
            rover.writeSystemLog(InstructionPayloadOuterClass.InstructionPayload.parseFrom(message), rover
                    .getInstructionQueue().size());
        } catch (InvalidProtocolBufferException ipe) {
            rover.writeErrorLog("Invalid Protocol Buffer Exception", ipe);
        }
    }

    @Override
    public String getStateName() {
        return "Hibernating State";
    }

    public void transmitMessage(byte[] message) {
        logger.debug("Can not transmit message in hibernating state.");
    }

    public void exploreArea() {
        logger.debug("Can not explore area in hibernating state.");
    }

    public void activateCamera() {
        logger.debug("Can not activate camera in hibernating state.");
    }

    public void move(InstructionPayloadOuterClass.InstructionPayload payload) {
        logger.debug("Can not move in hibernating state.");
    }

    public void hibernate() {
        logger.debug("Already in hibernating state.");
    }

    public void rechargeBattery() {
        logger.debug("Should already be recharging the battery");
    }

    public void scanSurroundings() {
        logger.debug("Can not scan surroundings in hibernating state.");
    }

    public void performDiagnostics() {
        logger.debug("Diagnostics disabled in hibernating state.");
    }

    public void performRadarScan() {
        logger.debug("Can not perform radar scan in hibernating state.");
    }

    @Override
    public void sleep() {

    }

    @Override
    public void wakeUp() {

    }
}
