package space.exploration.mars.rover.kernel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.InstructionPayloadOuterClass;
import space.exploration.communications.protocol.service.WeatherQueryOuterClass;
import space.exploration.mars.rover.animation.WeatherAnimationEngine;

public class WeatherSensingState implements State {
    private Logger logger = LoggerFactory.getLogger(WeatherSensingState.class);
    private Rover  rover  = null;

    public WeatherSensingState(Rover rover) {
        this.rover = rover;
    }

    @Override
    public void receiveMessage(byte[] message) {
        logger.info("Adding message to the instruction Queue, current length = " + rover.getInstructionQueue().size());
    }

    @Override
    public void transmitMessage(byte[] message) {

    }

    @Override
    public void exploreArea() {

    }

    @Override
    public void move(InstructionPayloadOuterClass.InstructionPayload.TargetPackage targetPackage) {
        logger.debug("Can not move in " + getStateName() );
    }

    @Override
    public void hibernate() {

    }

    @Override
    public void senseWeather(WeatherQueryOuterClass.WeatherQuery weatherQuery) {
        logger.info("Will get mars weather measurements");
        try {
            WeatherAnimationEngine weatherAnimationEngine = rover.getMarsArchitect().getWeatherEngine();
            weatherAnimationEngine.updateLocation(rover.getMarsArchitect().getRobot().getLocation());
            weatherAnimationEngine.renderWeatherAnimation();
            rover.setState(rover.getTransmittingState());

            if (weatherQuery == null) {
                rover.transmitMessage(rover.getWeatherSensor().getWeather());
            } else {
                rover.transmitMessage(rover.getWeatherSensor().getWeather(weatherQuery));
            }
        } catch (Exception e) {
            logger.error("Error while requesting weather data.", e);
            rover.setState(rover.getListeningState());
        }
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
