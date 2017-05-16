package space.exploration.mars.rover.bootstrap;

import space.exploration.mars.rover.communication.Receiver;

public class MatrixListeningPod {
    public static void main(String[] args) {
        System.out.println("Listening to the empty void of space .. ");
        Receiver soundPod;
        try {
            soundPod = new Receiver();
            soundPod.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
