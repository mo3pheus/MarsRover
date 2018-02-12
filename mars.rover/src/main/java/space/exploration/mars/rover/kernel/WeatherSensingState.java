package space.exploration.mars.rover.kernel;

import com.google.protobuf.InvalidProtocolBufferException;
import com.yammer.metrics.core.Meter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.InstructionPayloadOuterClass;
import space.exploration.communications.protocol.service.WeatherQueryOuterClass;
import space.exploration.mars.rover.animation.WeatherAnimationEngine;
import space.exploration.mars.rover.utils.RoverUtil;

import java.util.concurrent.TimeUnit;

public class WeatherSensingState implements State {
    private Meter  requests = null;
    private Logger logger   = LoggerFactory.getLogger(WeatherSensingState.class);
    private Rover  rover    = null;

    public WeatherSensingState(Rover rover) {
        this.rover = rover;
        requests = this.rover.getMetrics().newMeter(WeatherSensingState.class, getStateName(), "requests", TimeUnit
                .HOURS);
    }

    @Override
    public void receiveMessage(byte[] message) {
        logger.info("Adding message to the instruction Queue, current length = " + rover.getInstructionQueue().size());
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
    public void transmitMessage(byte[] message) {

    }

    @Override
    public void exploreArea() {

    }

    @Override
    public void synchronizeClocks(String utcTime) {
        logger.debug("Can not sync clocks in " + getStateName());
    }

    @Override
    public void move(InstructionPayloadOuterClass.InstructionPayload.TargetPackage targetPackage) {
        logger.debug("Can not move in " + getStateName());
    }

    @Override
    public void shootNeutrons() {
    }

    @Override
    public void hibernate() {

    }

    @Override
    public Meter getRequests() {
        return requests;
    }

    @Override
    public void senseWeather(WeatherQueryOuterClass.WeatherQuery weatherQuery) {
        requests.mark();
        rover.reflectRoverState();
        logger.info("Will get mars weather measurements");
        try {
            WeatherAnimationEngine weatherAnimationEngine = rover.getMarsArchitect().getWeatherEngine();
            weatherAnimationEngine.updateLocation(rover.getMarsArchitect().getRobot().getLocation());
            weatherAnimationEngine.renderWeatherAnimation();

            byte[] weatherData = rover.getWeatherSensor().getWeather();
            rover.setState(rover.getTransmittingState());
            rover.transmitMessage(weatherData);
        } catch (Exception e) {
            logger.error("Error while requesting weather data.", e);
            rover.setState(rover.getListeningState());
        }
    }

    @Override
    public void gracefulShutdown() {
        logger.error(" Can not perform gracefulShutdown while in " + getStateName());
    }

    @Override
    public void scanSurroundings() {

    }

    @Override
    public void activateCameraById(String camId) {

    }

    @Override
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

    @Override
    public String getStateName() {
        return "Weather Sensing State";
    }
}
