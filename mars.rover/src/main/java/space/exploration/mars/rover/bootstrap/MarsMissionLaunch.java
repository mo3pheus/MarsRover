package space.exploration.mars.rover.bootstrap;

import java.io.IOException;

import space.exploration.mars.rover.kernel.Rover;

public class MarsMissionLaunch {

	public static void main(String[] args) {
		try {
			new Rover(MatrixCreation.getMatrixConfig(), MatrixCreation.getComsConfig());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
