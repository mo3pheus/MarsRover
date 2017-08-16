/**
 *
 */
package space.exploration.mars.rover.kernel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.robot.RobotPositionsOuterClass.RobotPositions;

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
        // TODO Auto-generated method stub
        System.out.println("In hibernating state's receiveMessage() ");
        try {
            rover.getInstructionQueue().add(message);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
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

    public void move(RobotPositions positions) {
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
