/**
 * 
 */
package space.exploration.mars.rover.kernel;

import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.robot.RobotPositionsOuterClass.RobotPositions;
import space.exploration.mars.rover.communication.RoverStatusOuterClass.RoverStatus;
import space.exploration.mars.rover.communication.RoverStatusOuterClass.RoverStatus.Location;
import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.sensor.Spectrometer;

/**
 * @author sanketkorgaonkar
 *
 */
public class ExploringState implements State {

	private Rover rover = null;

	public ExploringState(Rover rover) {
		this.rover = rover;
	}

	public void receiveMessage(byte[] message) {
	}

	public void transmitMessage(byte[] message) {
	}

	public void exploreArea() {
		MarsArchitect marsArchitect = rover.getMarsArchitect();
		Cell robot = marsArchitect.getRobot();
		Spectrometer spectrometer = new Spectrometer(robot.getLocation());
		spectrometer.setCellWidth(marsArchitect.getCellWidth());
		spectrometer.setSurfaceComp(marsArchitect.getSoilCompositionMap());
		spectrometer.processSurroundingArea();

		marsArchitect.setSpectrometerAnimationEngine(spectrometer);
		marsArchitect.getSpectrometerAnimationEngine().activateSpectrometer();
		marsArchitect.returnSurfaceToNormal();

		Location.Builder lBuilder = Location.newBuilder().setX(robot.getLocation().x).setY(robot.getLocation().y);

		RoverStatus.Builder rBuilder = RoverStatus.newBuilder();
		RoverStatus status = rBuilder.setBatteryLevel(rover.getBatter().getPrimaryPowerUnits())
				.setSolNumber(rover.getSol()).setLocation(lBuilder.build()).setNotes("Spectroscope engaged!")
				.setModuleMessage(spectrometer.getSpectrometerReading().toByteString())
				.setScet(System.currentTimeMillis()).setModuleReporting(ModuleDirectory.Module.SCIENCE.getValue())
				.build();

		rover.state = rover.transmittingState;
		rover.transmitMessage(status.toByteArray());
	}

	public void performExperiments() {
	}

	public void move(RobotPositions positions) {
	}

	public void hibernate() {
	}

	public void rechargeBattery() {
	}

	public void scanSurroundings() {
	}

	public void performDiagnostics() {
	}

}
