package space.exploration.mars.rover.kernel;

import space.exploration.mars.rover.robot.RobotPositionsOuterClass.RobotPositions;

public interface State {
	public void receiveMessage(byte[] message);

	public void transmitMessage(byte[] message);

	public void exploreArea();

	public void activateCamera();

	public void move(RobotPositions positions);

	public void hibernate();

	public void rechargeBattery();

	public void scanSurroundings();

	public void performDiagnostics();
}
