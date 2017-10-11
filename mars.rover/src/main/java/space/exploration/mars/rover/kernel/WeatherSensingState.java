package space.exploration.mars.rover.kernel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.InstructionPayloadOuterClass;

public class WeatherSensingState implements State {
    private Logger logger = LoggerFactory.getLogger(WeatherSensingState.class);
    private Rover  rover  = null;

    @Override
    public void receiveMessage(byte[] message) {
        logger.info("Adding message to the insturction Queue, current length = " + rover.getInstructionQueue().size());
    }

    @Override
    public void transmitMessage(byte[] message) {

    }

    @Override
    public void exploreArea() {

    }

    @Override
    public void activateCamera() {

    }

    @Override
    public void move(InstructionPayloadOuterClass.InstructionPayload payload) {

    }

    @Override
    public void hibernate() {

    }

    @Override
    public void senseWeather() {
        logger.info("Will get mars weather measurements");
        byte[] weatherReport = rover.getWeatherSensor().getWeather();
        rover.setState(rover.getTransmittingState());
        rover.transmitMessage(weatherReport);
    }

    @Override
    public void scanSurroundings() {

    }

    @Override
    public void performDiagnostics() {

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
    public String getStateName() {
        return "Weather Sensing State";
    }

    public WeatherSensingState(Rover rover) {
        this.rover = rover;
    }
}
