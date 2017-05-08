/**
 * 
 */
package space.exploration.mars.rover.kernel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.robot.RobotPositionsOuterClass.RobotPositions;

/**
 * @author sanketkorgaonkar
 *
 */
public class HibernatingState implements State {
	private Rover rover = null;
	private Logger logger = LoggerFactory.getLogger(HibernatingState.class);

	public HibernatingState(Rover rover) {
		this.rover = rover;
	}

	public void receiveMessage(byte[] message) {
		// TODO Auto-generated method stub
        System.out.println("In hibernating state's receiveMessage() ");
        try {
            rover.getInstructionQueue().add(message);
        } catch(Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }
	}

	public void transmitMessage(byte[] message) {
		// TODO Auto-generated method stub

	}

	public void exploreArea() {
		// TODO Auto-generated method stub

	}

	public void performExperiments() {
		// TODO Auto-generated method stub

	}

	public void move(RobotPositions positions) {
		// TODO Auto-generated method stub

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

}
