package space.exploration.mars.rover.kernel;

public class Rover {
	public final String	ROVER_NAME	= "Curiosity";

	/* Rover technology stack */
	Battery				battery		= new Battery();

	/* States supported */
	State				isStationaryReceivingMessage;
	State				isStationarySensing;
	State				isMoving;
	State				isStationaryExploring;
	State				isStationaryScanning;
	State				isStationaryTransmitting;
	State				isStationaryHibernating;
	State				isStationaryRecharging;

	public Battery getBatter() {
		return battery;
	}
}
