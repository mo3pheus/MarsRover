package space.exploration.mars.rover.bootstrap;

import space.exploration.mars.rover.communication.Transmitter;
import space.exploration.mars.rover.communication.Transmitter.KafkaShipmentBuilder;

import java.util.Scanner;

public class CommandControl {
    public static void main(String[] args) throws Exception {
        String path = CommandControl.class.getResource("/kafka.properties").getPath();
        KafkaShipmentBuilder builder = new KafkaShipmentBuilder().withPropertyFileAt(path)
                .withSourceTopic("nebuchadnezzar.main.deck.com.2").build();
        Transmitter zionComs = new Transmitter(builder);

        int choice = 0;
        while (choice != 2) {
            System.out.println("0. Send Message");
            System.out.println("1. Send partialMessages");
            System.out.println("2. Exit");
            System.out.println("Please enter your choice");
            Scanner scanner = new Scanner(System.in);
            choice = scanner.nextInt();

            switch (choice) {
                case 0: {
                    zionComs.sendMessage();
                }
                break;
                case 1: {
                    zionComs.sendMessages();
                }
                break;
                case 2: {
                    scanner.close();
                    System.out.println("This is Zion coms signing off.");
                }
                break;
                default: {
                }
            }
        }
    }

}
