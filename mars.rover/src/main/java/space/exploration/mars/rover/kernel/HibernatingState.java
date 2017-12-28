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

import java.util.concurrent.TimeUnit;

/**
 * @author sanketkorgaonkar
 */
public class HibernatingState implements State {
    private Meter  requests = null;
    private Rover  rover    = null;
    private Logger logger   = LoggerFactory.getLogger(HibernatingState.class);

    public HibernatingState(Rover rover) {
        this.rover = rover;
        requests = this.rover.getMetrics().newMeter(HibernatingState.class, getStateName(), "requests", TimeUnit
                .HOURS);
    }

    public void receiveMessage(byte[] message) {
        logger.info("In hibernating state's receiveMessage(), adding message to instruction queue");
        rover.getInstructionQueue().add(message);
        try {
            rover.writeSystemLog(InstructionPayloadOuterClass.InstructionPayload.parseFrom(message), rover
                    .getInstructionQueue().size());
        } catch (InvalidProtocolBufferException ipe) {
            rover.writeErrorLog("Invalid Protocol Buffer Exception", ipe);
        }
    }

    @Override
    public void activateCameraById(String camId) {

    }

    @Override
    public Meter getRequests() {
        return requests;
    }

    @Override
    public void gracefulShutdown() {
        logger.error(" Can not perform gracefulShutdown while in " + getStateName());
    }

    @Override
    public String getStateName() {
        return "Hibernating State";
    }

    public void transmitMessage(byte[] message) {
        logger.debug("Can not transmit message in hibernating state.");
    }

    public void exploreArea() {
        logger.debug("Can not explore area in hibernating state.");
    }

    public void move(InstructionPayloadOuterClass.InstructionPayload.TargetPackage targetPackage) {
        logger.debug("Can not move in " + getStateName());
    }

    public void hibernate() {
        logger.debug("Already in hibernating state.");
        requests.mark();
    }

    public void senseWeather(WeatherQueryOuterClass.WeatherQuery weatherQuery) {
        logger.debug("Should already be recharging the battery");
    }

    public void scanSurroundings() {
        logger.debug("Can not scan surroundings in hibernating state.");
    }

    public void activateCameraById() {
        logger.debug("Diagnostics disabled in hibernating state.");
    }

    public void performRadarScan() {
        logger.debug("Can not perform radar scan in hibernating state.");
    }

    @Override
    public void sleep() {

    }

    @Override
    public void wakeUp() {

    }

    @Override
    public void getSclkInformation() {
        logger.error("Can not get sclkInformation while in hibernatingState");
    }

    @Override
    public void synchronizeClocks(String utcTime) {
        logger.debug("Can not sync clocks in " + getStateName());
    }
}
