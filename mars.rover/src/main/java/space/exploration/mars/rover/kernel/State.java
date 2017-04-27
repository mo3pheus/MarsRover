package space.exploration.mars.rover.kernel;

public interface State {
	public void receiveMessage(byte[] message);

	public void transmitMessage(byte[] message);

	public void exploreArea();

	public void performExperiments();

	public void move();

	public void hibernate();

	public void rechargeBattery();

	public void scanSurroundings();

	public void performDiagnostics();
}
