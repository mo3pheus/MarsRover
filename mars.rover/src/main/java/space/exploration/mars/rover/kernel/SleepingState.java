package space.exploration.mars.rover.kernel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.InstructionPayloadOuterClass;
import space.exploration.communications.protocol.service.WeatherQueryOuterClass;
import space.exploration.mars.rover.animation.SleepingAnimationEngine;
import space.exploration.mars.rover.environment.EnvironmentUtils;

public class SleepingState implements State {
    private Logger                  logger                  = LoggerFactory.getLogger(SleepingState.class);
    private Rover                   rover                   = null;
    private SleepingAnimationEngine sleepingAnimationEngine = null;

    public SleepingState(Rover rover) {
        this.rover = rover;
        sleepingAnimationEngine = new SleepingAnimationEngine(this.rover);
    }

    @Override
    public void receiveMessage(byte[] message) {
        logger.info("Awaking from sleeping state");
        sleepingAnimationEngine.wakeupRover();
        rover.getMarsArchitect().getRobot().setColor(EnvironmentUtils.findColor(rover.getMarsConfig()
                                                                                        .getProperty
                                                                                                (EnvironmentUtils
                                                                                                         .ROBOT_COLOR)));
        rover.getMarsArchitect().returnSurfaceToNormal();
        rover.setState(rover.getListeningState());
        rover.receiveMessage(message);
    }

    @Override
    public void synchronizeClocks(String utcTime) {
        logger.debug("Can not sync clocks in " + getStateName());
    }

    @Override
    public void activateCameraById(String camId) {

    }

    @Override
    public void transmitMessage(byte[] message) {
        logger.error("Can not transmit from sleeping state");
    }

    @Override
    public void exploreArea() {
        logger.error("Can not run experiments from sleeping state");
    }

    @Override
    public void move(InstructionPayloadOuterClass.InstructionPayload.TargetPackage targetPackage) {
        logger.debug("Can not move in " + getStateName());
    }

    @Override
    public void hibernate() {
        logger.error("Rover should not go into hibernating state from sleeping state");
    }

    @Override
    public void gracefulShutdown() {
        logger.error(" Can not perform gracefulShutdown while in " + getStateName());
    }

    @Override
    public void senseWeather(WeatherQueryOuterClass.WeatherQuery weatherQuery) {
        logger.error("Rover conserves battery but does not recharge batteries in sleeping state");
    }

    @Override
    public void scanSurroundings() {
        logger.error("Can not run experiments(scan surroundings) from sleeping state");
    }

    @Override
    public void performRadarScan() {
        logger.error("Can not perform radar scans when sleeping");
    }

    @Override
    public void sleep() {
        sleepingAnimationEngine = new SleepingAnimationEngine(rover);
        sleepingAnimationEngine.sleep();
    }

    @Override
    public String getStateName() {
        return "Sleeping State";
    }

    @Override
    public void wakeUp() {
        sleepingAnimationEngine.wakeupRover();
        rover.getMarsArchitect().returnSurfaceToNormal();
        rover.setState(rover.getListeningState());
        rover.setTimeMessageReceived(System.currentTimeMillis());
    }

    @Override
    public void getSclkInformation() {

    }
}
