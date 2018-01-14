package space.exploration.mars.rover.kernel;

import com.yammer.metrics.core.Meter;
import space.exploration.communications.protocol.InstructionPayloadOuterClass.InstructionPayload.TargetPackage;
import space.exploration.communications.protocol.service.DanRDRData;
import space.exploration.communications.protocol.service.WeatherQueryOuterClass;

public interface State {
    void receiveMessage(byte[] message);

    void transmitMessage(byte[] message);

    void exploreArea();

    void move(TargetPackage payload);

    void hibernate();

    void senseWeather(WeatherQueryOuterClass.WeatherQuery weatherQuery);

    void scanSurroundings();

    void activateCameraById(String camId);

    void performRadarScan();

    void sleep();

    void wakeUp();

    void getSclkInformation();

    String getStateName();

    void synchronizeClocks(String utcTime);

    void gracefulShutdown();

    Meter getRequests();

    void shootNeutrons();
}
