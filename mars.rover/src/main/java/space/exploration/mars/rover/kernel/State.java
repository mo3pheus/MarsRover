package space.exploration.mars.rover.kernel;

import space.exploration.mars.rover.InstructionPayloadOuterClass;

public interface State {
    void receiveMessage(byte[] message);

    void transmitMessage(byte[] message);

    void exploreArea();

    void activateCamera();

    void move(InstructionPayloadOuterClass.InstructionPayload payload);

    void hibernate();

    void rechargeBattery();

    void scanSurroundings();

    void performDiagnostics();

    void performRadarScan();

    void sleep();

    void wakeUp();

    String getStateName();
}
