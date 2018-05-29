/**
 *
 */
package space.exploration.mars.rover.kernel;

import com.google.protobuf.InvalidProtocolBufferException;
import com.yammer.metrics.core.Meter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.InstructionPayloadOuterClass;
import space.exploration.communications.protocol.service.WeatherQueryOuterClass;
import space.exploration.communications.protocol.softwareUpdate.SwUpdatePackageOuterClass;
import space.exploration.mars.rover.animation.RadioAnimationEngine;
import space.exploration.mars.rover.utils.RoverUtil;

import java.util.concurrent.TimeUnit;

/**
 * @author sanketkorgaonkar
 */
public class TransmittingState implements State {
    private Meter  requests = null;
    private Rover  rover    = null;
    private Logger logger   = LoggerFactory.getLogger(TransmittingState.class);

    public TransmittingState(Rover rover) {
        this.rover = rover;
        requests = this.rover.getMetrics().newMeter(TransmittingState.class, getStateName(), "requests", TimeUnit
                .HOURS);
    }

    public void receiveMessage(byte[] message) {
        rover.reflectRoverState();
        rover.getInstructionQueue().add(message);
        try {
            RoverUtil.writeSystemLog(rover, InstructionPayloadOuterClass.InstructionPayload.parseFrom(message), rover
                    .getInstructionQueue().size());
        } catch (InvalidProtocolBufferException ipe) {
            RoverUtil.writeErrorLog(rover, "Invalid Protocol Buffer Exception", ipe);
        }
    }

    @Override
    public void shootNeutrons() {
    }

    @Override
    public void updateSoftware(SwUpdatePackageOuterClass.SwUpdatePackage swUpdatePackage) {
        logger.error("Can not update software in " + getStateName());
    }

    @Override
    public Meter getRequests() {
        return requests;
    }

    @Override
    public void synchronizeClocks(String utcTime) {
        logger.debug("Can not sync clocks in " + getStateName());
    }

    public void transmitMessage(byte[] message) {
        requests.mark();
        rover.reflectRoverState();
        RadioAnimationEngine radioAnimationEngine = new RadioAnimationEngine(rover.getMarsConfig(), rover
                .getMarsArchitect().getMarsSurface(), rover.getMarsArchitect().getRobot(), true);
        radioAnimationEngine.activateRadio();
        rover.getRadio().sendMessage(message);
        rover.getMarsArchitect().returnSurfaceToNormal();
        rover.state = rover.listeningState;
        rover.reflectRoverState();
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
