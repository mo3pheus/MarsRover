/**
 *
 */
package space.exploration.mars.rover.kernel;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.InstructionPayloadOuterClass;
import space.exploration.communications.protocol.service.WeatherQueryOuterClass;
import space.exploration.mars.rover.animation.RadioAnimationEngine;

/**
 * @author sanketkorgaonkar
 */
public class TransmittingState implements State {
    private Rover  rover  = null;
    private Logger logger = LoggerFactory.getLogger(TransmittingState.class);

    public TransmittingState(Rover rover) {
        this.rover = rover;
    }

    public void receiveMessage(byte[] message) {
        rover.getInstructionQueue().add(message);
        try {
            rover.writeSystemLog(InstructionPayloadOuterClass.InstructionPayload.parseFrom(message), rover
                    .getInstructionQueue().size());
        } catch (InvalidProtocolBufferException ipe) {
            rover.writeErrorLog("InvalidProtocolBufferException", ipe);
        }
    }

    @Override
    public void synchronizeClocks(String utcTime) {
        logger.debug("Can not sync clocks in " + getStateName());
    }

    public void transmitMessage(byte[] message) {
        RadioAnimationEngine radioAnimationEngine = new RadioAnimationEngine(rover.getMarsConfig(), rover
                .getMarsArchitect().getMarsSurface(), rover.getMarsArchitect().getRobot(), true);
        radioAnimationEngine.activateRadio();
        rover.getRadio().sendMessage(message);
        rover.getMarsArchitect().returnSurfaceToNormal();
        rover.state = rover.listeningState;
    }

    @Override
    public void activateCameraById(String camId) {
    }

    @Override
    public String getStateName() {
        return "Transmitting State";
    }

    public void exploreArea() {
    }

    @Override
    public void gracefulShutdown() {
        logger.error(" Can not perform gracefulShutdown while in " + getStateName());
    }

    public void move(InstructionPayloadOuterClass.InstructionPayload.TargetPackage targetPackage) {
        logger.debug("Can not move in " + getStateName());
    }

    public void hibernate() {
    }

    public void senseWeather(WeatherQueryOuterClass.WeatherQuery weatherQuery) {
    }

    public void scanSurroundings() {
    }

    public void activateCameraById() {
    }

    public void performRadarScan() {
    }

    @Override
    public void sleep() {
    }

    @Override
    public void wakeUp() {
    }

    @Override
    public void getSclkInformation() {

    }
}
