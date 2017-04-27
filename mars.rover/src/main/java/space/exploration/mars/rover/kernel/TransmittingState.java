/**
 * 
 */
package space.exploration.mars.rover.kernel;

/**
 * @author sanketkorgaonkar
 *
 */
public class TransmittingState implements State {
	private Rover rover = null;
	
	public TransmittingState(Rover rover){
		this.rover = rover;
	}

	public void receiveMessage(byte[] message) {
		// TODO Auto-generated method stub

	}

	public void transmitMessage(byte[] message) {
		// TODO Auto-generated method stub
		rover.getRadio().sendMessage(message);
		rover.state = rover.listeningState;
	}

	public void exploreArea() {
		// TODO Auto-generated method stub

	}

	public void performExperiments() {
		// TODO Auto-generated method stub

	}

	public void move() {
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
