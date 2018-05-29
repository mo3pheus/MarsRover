package space.exploration.mars.rover.kernel;

import com.yammer.metrics.core.Meter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.InstructionPayloadOuterClass;
import space.exploration.communications.protocol.communication.RoverStatusOuterClass;
import space.exploration.communications.protocol.service.WeatherQueryOuterClass;
import space.exploration.communications.protocol.softwareUpdate.SwUpdatePackageOuterClass;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.utils.RoverUtil;

import java.io.IOException;

public class MaintenanceState implements State {
    private Logger logger = LoggerFactory.getLogger(ListeningState.class);
    private Rover  rover  = null;

    public MaintenanceState(Rover rover) {
        this.rover = rover;
    }

    @Override
    public void receiveMessage(byte[] message) {

    }

    @Override
    public void transmitMessage(byte[] message) {

    }

    @Override
    public void exploreArea() {

    }

    @Override
    public void move(InstructionPayloadOuterClass.InstructionPayload.TargetPackage payload) {

    }

    @Override
    public void hibernate() {

    }

    @Override
    public void senseWeather(WeatherQueryOuterClass.WeatherQuery weatherQuery) {

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
        return "Maintenance State";
    }

    @Override
    public void synchronizeClocks(String utcTime) {

    }

    @Override
    public void gracefulShutdown() {
        rover.reflectRoverState();
        logger.info("Rover initiating a gracefulShutdown.");
        rover.getMarsArchitect().getRobot().setColor(EnvironmentUtils.findColor("robotShutdownMode"));
        rover.getMarsArchitect().getMarsSurface().repaint();

        logger.info("Synchronizing Clocks.");
        rover.state = rover.sclkBeepingState;
        rover.synchronizeClocks(rover.getSpacecraftClock().getUTCTime());

        logger.info("Sending diagnostic heartbeat.");
        rover.getPacemaker().sendHeartBeat(true);

        logger.info("Severing ties with the rover.");
        rover.shutdownRover();

        if (rover.isGracefulShutdown()) {
            logger.info("No pre-existing launcher process found. Shutting down system.");
            rover.shutdownSystem();
        }
    }

    @Override
    public Meter getRequests() {
        return null;
    }

    @Override
    public void shootNeutrons() {

    }

    @Override
    public void updateSoftware(SwUpdatePackageOuterClass.SwUpdatePackage swUpdatePackage) {
        rover.reflectRoverState();
        logger.info("Rover commencing software update.");

        double newVersion = swUpdatePackage.getVersion();
        if (newVersion > rover.getSoftwareVersion()) {
            logger.info("Old software version = " + rover.getSoftwareVersion() + " upgrading to " + newVersion);
            RoverUtil.processSoftwareUpdate(rover, swUpdatePackage);
        }

        logger.info("Sending software update complete message.");
        rover.state = rover.transmittingState;
        RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverUtil.generateUpdateStatus();
        rover.transmitMessage(rBuilder.build().toByteArray());

        try {
            String[] commands       = {"./" + swUpdatePackage.getScriptFileName()};
            Process  process        = Runtime.getRuntime().exec(commands);
            Process  currentProcess = rover.getLauncherProcess();

            if (currentProcess != null && currentProcess.isAlive()) {
                logger.info("Destroying old launcher process and replacing with new.");
                rover.setLauncherProcess(process);
                currentProcess.destroy();
            }
        } catch (IOException e) {
            logger.error("Error while launching new launch script.", e);
        }

        logger.info("Commencing rover shutdown");
        rover.state = rover.maintenanceState;
        rover.gracefulShutdown();
    }
}
