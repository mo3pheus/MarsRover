package space.exploration.mars.rover.kernel;


public class Listening implements State {

	private Rover rover = null;

	public Listening(Rover rover) {
		this.rover = rover;
	}

	public void receiveMessage() {
		System.out.println(
				"Rover " + rover.ROVER_NAME + " has received message and will now act upon it - come what may!");
		System.out.println("What state the rover goes to next is completely dependent on the instructions");
		System.out.println(
				"First thing I will do is check if the powerRequested by the instructionPayload can be supported");

	}

	public void transmitMessage() {
		// TODO Auto-generated method stub

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
