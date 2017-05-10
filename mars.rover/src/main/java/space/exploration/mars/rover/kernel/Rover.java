package space.exploration.mars.rover.kernel;

import space.exploration.mars.rover.communication.Radio;
import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.power.Battery;
import space.exploration.mars.rover.robot.RobotPositionsOuterClass.RobotPositions;
import space.exploration.mars.rover.sensor.Lidar;
import space.exploration.mars.rover.sensor.Spectrometer;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import space.exploration.mars.rover.communication.RoverStatusOuterClass.RoverStatus;
import space.exploration.mars.rover.communication.RoverStatusOuterClass.RoverStatus.Location;

public class Rover {
	public static final String ROVER_NAME = "Curiosity";

	/* Configuration */
	Properties		marsConfig		= null;
	Properties		comsConfig		= null;
	MarsArchitect	marsArchitect	= null;
	State			state			= null;

	/* Equipment Stack */
	Battery			battery			= new Battery();
	Radio			radio			= null;

	/* States supported */
	State	listeningState;
	State	sensingState;
	State	movingState;
	State	exploringState;
	State	transmittingState;
	State	hibernatingState;
	State	rechargingState;

	/* Status messages */
	RoverStatus	status			= null;
	long		creationTime	= 0l;

	/* Sets up the rover and the boot-up sequence */
	public Rover(Properties marsConfig, Properties comsConfig) {
		this.creationTime = System.currentTimeMillis();
		this.marsConfig = marsConfig;
		this.comsConfig = comsConfig;
		this.listeningState = new ListeningState(this);
		this.hibernatingState = new HibernatingState(this);
		this.exploringState = new ExploringState(this);
		this.movingState = new MovingState(this);
		this.rechargingState = new RechargingState(this);
		this.sensingState = new SensingState(this);
		this.transmittingState = new TransmittingState(this);
		this.radio = new Radio(comsConfig, this);
		this.marsArchitect = new MarsArchitect(marsConfig);
		state = transmittingState;
		transmitMessage(getBootupMessage());
	}

	public void receiveMessage(byte[] message) {
		state.receiveMessage(message);
	}

	public void scanSurroundings() {
		state.scanSurroundings();
	}

	public void move(RobotPositions positions) {
		state.move(positions);
	}

	public void exploreArea() {
		state.exploreArea();
	}

	public void transmitMessage(byte[] message) {
		state.transmitMessage(message);
	}

	public Radio getRadio() {
		return radio;
	}

	public MarsArchitect getMarsArchitect() {
		return marsArchitect;
	}

	public void setStatus(RoverStatus status) {
		this.status = status;
	}

	public Properties getMarsConfig() {
		return marsConfig;
	}

	public long getOneSolDuration() {
		/* Time scaled by a factor of 60. */
		long time = TimeUnit.MINUTES.toMillis(24);
		time += TimeUnit.SECONDS.toMillis(39);
		time += 35;
		time += 244;
		return time;
	}

	public Battery getBatter() {
		return battery;
	}

	public int getSol() {
		long diff = System.currentTimeMillis() - creationTime;
		long solMs = getOneSolDuration();
		return Math.round(diff / solMs);
	}

	private byte[] getBootupMessage() {
		Location location = Location.newBuilder().setX(marsArchitect.getRobot().getLocation().x)
				.setY(marsArchitect.getRobot().getLocation().y).build();
		RoverStatus.Builder rBuilder = RoverStatus.newBuilder();
		rBuilder.setModuleReporting(ModuleDirectory.Module.KERNEL.getValue());
		rBuilder.setScet(System.currentTimeMillis());
		rBuilder.setLocation(location);
		rBuilder.setBatteryLevel(this.getBatter().getPrimaryPowerUnits());
		rBuilder.setSolNumber(getSol());
		rBuilder.setNotes("Rover " + ROVER_NAME + " reporting to earth after a restart - How are you earth?");
		return rBuilder.build().toByteArray();
	}
}
