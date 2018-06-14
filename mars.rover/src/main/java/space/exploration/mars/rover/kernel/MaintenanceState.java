package space.exploration.mars.rover.kernel;

import com.google.protobuf.ByteString;
import com.yammer.metrics.core.Meter;
import communications.protocol.ModuleDirectory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.InstructionPayloadOuterClass;
import space.exploration.communications.protocol.communication.RoverStatusOuterClass;
import space.exploration.communications.protocol.service.WeatherQueryOuterClass;
import space.exploration.communications.protocol.softwareUpdate.SwUpdatePackageOuterClass;
import space.exploration.kernel.diagnostics.LogRequest;
import space.exploration.kernel.diagnostics.LogResponse;
import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.utils.FileUtil;
import space.exploration.mars.rover.utils.RoverUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    public void requestLogs(LogRequest.LogRequestPacket logRequestPacket) {
        List<LogResponse.LogResponsePacket.LogFile> logContents = new ArrayList<>();

        logger.info("Requesting logs in date format = " + logRequestPacket.getDateFormat());
        logger.info("Start date = " + logRequestPacket.getStartDate() + " End date = " + logRequestPacket.getEndDate());
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(logRequestPacket.getDateFormat());
        DateTime          startTime         = dateTimeFormatter.parseDateTime(logRequestPacket.getStartDate());
        DateTime          endTime           = dateTimeFormatter.parseDateTime(logRequestPacket.getEndDate());

        try {
            List<File> logFiles = FileUtil.getLogFiles(startTime, endTime, rover.getRoverConfig()
                    .getLogArchiveLocation());
            for (int i = 0; i < logFiles.size(); i++) {
                File logFile = logFiles.get(i);
                logContents.add(buildLogFile(logFile.getName(), FileUtil.getFileContent(logFile)));
            }
        } catch (IOException io) {
            logger.error("IOException while retrieving log files. Requested startDate = " + startTime + " endDate = "
                                 + endTime, io);
        }

        LogResponse.LogResponsePacket.Builder logBuilder = LogResponse.LogResponsePacket.newBuilder();
        logBuilder.setStartDate(startTime.toString());
        logBuilder.setEndDate(endTime.toString());
        logBuilder.addAllLogFiles(logContents);

        Cell robot = rover.getMarsArchitect().getRobot();
        RoverStatusOuterClass.RoverStatus.Location.Builder lBuilder = RoverStatusOuterClass.RoverStatus.Location
                .newBuilder().setX(robot.getLocation().x).setY(robot.getLocation
                        ().y);

        RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverStatusOuterClass.RoverStatus.newBuilder();
        RoverStatusOuterClass.RoverStatus status = rBuilder.setBatteryLevel(rover.getBattery().getPrimaryPowerUnits())
                .setSolNumber(rover.getSpacecraftClock().getSol())
                .setLocation(lBuilder.build()).setNotes("Rover logs requested.")
                .setModuleMessage(ByteString.copyFrom(logBuilder.build().toByteArray()))
                .setSCET(System.currentTimeMillis()).setModuleReporting(ModuleDirectory.Module.KERNEL
                                                                                .getValue())
                .build();

        rover.state = rover.transmittingState;
        rover.transmitMessage(status.toByteArray());
    }

    private LogResponse.LogResponsePacket.LogFile buildLogFile(String fileName, String content) {
        LogResponse.LogResponsePacket.LogFile.Builder lBuilder = LogResponse.LogResponsePacket.LogFile.newBuilder();
        lBuilder.setFilename(fileName);
        lBuilder.setContent(content);
        return lBuilder.build();
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
        if (newVersion > rover.getRoverConfig().getSoftwareVersion()) {
            logger.info("Old software version = " + rover.getRoverConfig().getSoftwareVersion() + " upgrading to " +
                                newVersion);
            RoverUtil.processSoftwareUpdate(rover, swUpdatePackage);
            rover.getRoverConfig().setSoftwareVersion(swUpdatePackage.getVersion());

            logger.info("Sending software update complete message.");
            rover.state = rover.transmittingState;
            RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverUtil.generateUpdateStatus();
            rover.transmitMessage(rBuilder.build().toByteArray());

            logger.info("Commencing rover shutdown");
            rover.state = rover.maintenanceState;
            rover.gracefulShutdown();

            try {
                String[] commands = {"./" + swUpdatePackage.getScriptFileName()};
                Process  process  = Runtime.getRuntime().exec(commands);
                rover.setLauncherProcess(process);

                commands[0] = "./smartTail.sh";
                Runtime.getRuntime().exec(commands);
                Thread.sleep(TimeUnit.MINUTES.toMillis(2l));
                rover.shutdownSystem();
            } catch (IOException e) {
                logger.error("Error while launching new launch script.", e);
            } catch (InterruptedException e) {
                logger.error("Interrupted Exception while waiting for process to kill runtime env.", e);
            }
        } else {
            logger.error("Software already up to date.");
            rover.state = rover.listeningState;
        }
    }
}
