package space.exploration.mars.rover.kernel;

import space.exploration.mars.rover.power.Battery;

public class Rover {
	public final String	ROVER_NAME	= "Curiosity";

	/* Graphics stack */
	
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
