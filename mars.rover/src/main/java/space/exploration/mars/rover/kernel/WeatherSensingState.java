package space.exploration.mars.rover.kernel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.InstructionPayloadOuterClass;
import space.exploration.mars.rover.communication.RoverStatusOuterClass;
import space.exploration.mars.rover.dataUplink.WeatherData;
import space.exploration.mars.rover.dataUplink.WeatherQueryService;
import space.exploration.mars.rover.utils.RoverUtil;

public class WeatherSensingState implements State {
    private Logger logger = LoggerFactory.getLogger(WeatherSensingState.class);
    private Rover  rover  = null;

    public WeatherSensingState(Rover rover) {
        this.rover = rover;
    }

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
        try {
            WeatherQueryService weatherQueryService = new WeatherQueryService();
            weatherQueryService.executeQuery();
            WeatherData.WeatherPayload weatherPayload = (WeatherData.WeatherPayload) weatherQueryService.getResponse();

            logger.debug(weatherPayload.toString());

            rover.setState(rover.getTransmittingState());

            RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverStatusOuterClass.RoverStatus.newBuilder();
            rBuilder.setBatteryLevel(rover.getBattery().getPrimaryPowerUnits());
            rBuilder.setModuleMessage(weatherPayload.toByteString());
            rBuilder.setModuleReporting(ModuleDirectory.Module.WEATHER_SENSOR.getValue());
            rBuilder.setLocation(RoverUtil.getLocation(rover.getMarsArchitect().getRobot().getLocation()));
            rBuilder.setNotes("Weather from Curiosity Actual");
            rBuilder.setSolNumber(weatherPayload.getSol());
            rBuilder.setSCET(System.currentTimeMillis());

            rover.transmitMessage(rBuilder.build().toByteArray());
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
    public String getStateName() {
        return "Weather Sensing State";
    }
}
