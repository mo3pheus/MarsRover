package space.exploration.mars.rover.bootstrap;

import space.exploration.mars.rover.communication.MatrixReceptor;

public class MatrixListeningPod {
	public static void main(String[] args) {
		System.out.println("Listening to the empty void of space .. ");
		MatrixReceptor soundPod;
		try {
			soundPod = new MatrixReceptor();
			soundPod.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
