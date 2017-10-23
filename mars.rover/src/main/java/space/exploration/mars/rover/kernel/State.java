package space.exploration.mars.rover.kernel;

import space.exploration.mars.rover.InstructionPayloadOuterClass;
import space.exploration.mars.rover.service.WeatherQueryOuterClass;

public interface State {
    void receiveMessage(byte[] message);

    void transmitMessage(byte[] message);

    void exploreArea();

    void move(InstructionPayloadOuterClass.InstructionPayload payload);

    void hibernate();

    void senseWeather(WeatherQueryOuterClass.WeatherQuery weatherQuery);

    void scanSurroundings();

    void activateCameraById(String camId);

    void performRadarScan();

    void sleep();

    void wakeUp();

    String getStateName();
}
