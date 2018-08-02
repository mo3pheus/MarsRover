package space.exploration.mars.rover.kernel;

import com.google.protobuf.ByteString;
import com.yammer.metrics.core.Meter;
import communications.protocol.ModuleDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.InstructionPayloadOuterClass;
import space.exploration.communications.protocol.communication.RoverStatusOuterClass;
import space.exploration.communications.protocol.service.SamQueryOuterClass;
import space.exploration.communications.protocol.service.WeatherQueryOuterClass;
import space.exploration.communications.protocol.softwareUpdate.SwUpdatePackageOuterClass;
import space.exploration.kernel.diagnostics.LogRequest;
import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.environment.MarsArchitect;

import java.util.concurrent.TimeUnit;

public class SampleAnalysisState implements State {
    private Rover rover    = null;
    private Meter requests = null;

    Logger logger = LoggerFactory.getLogger(SampleAnalysisState.class);


    public SampleAnalysisState(Rover rover) {
        this.rover = rover;
        requests = this.rover.getMetrics().newMeter(SampleAnalysisState.class, getStateName(), "requests", TimeUnit
                .HOURS);
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
        return "SampleAnalysisState";
    }

    @Override
    public void synchronizeClocks(String utcTime) {

    }

    @Override
    public void gracefulShutdown() {

    }

    @Override
    public Meter getRequests() {
        return this.requests;
    }

    @Override
    public void shootNeutrons() {

    }

    @Override
    public void updateSoftware(SwUpdatePackageOuterClass.SwUpdatePackage swUpdatePackage) {

    }

    @Override
    public void requestLogs(LogRequest.LogRequestPacket logRequestPacket) {

    }

    @Override
    public void sampleAnalysis(SamQueryOuterClass.SamQuery samQuery) {
        logger.info("Sample Analysis requested from MSL for sol = " + rover.getSpacecraftClock().getSol() + " " +
                            "samQuery = " + samQuery.toString());
        rover.reflectRoverState();

        if (!rover.getSamSensor().isCalibrated()) {
            logger.error("SamSensor not calibrated successfully. Returning rover to a stable state.");
            rover.state = rover.listeningState;
            return;
        }

        if (rover.getSamSensor().isEndOfLife()) {
            logger.error("SamSensor has reached endOfLife.");
            rover.state = rover.listeningState;
            return;
        }

        ByteString    samData       = rover.getSamSensor().getSamData();
        MarsArchitect marsArchitect = rover.getMarsArchitect();
        Cell          robot         = marsArchitect.getRobot();

        /* perform animations here. */
        marsArchitect.getSamAnimationEngine().renderAnimation();

        RoverStatusOuterClass.RoverStatus.Location.Builder lBuilder = RoverStatusOuterClass.RoverStatus.Location
                .newBuilder().setX(robot.getLocation().x).setY(robot.getLocation
                        ().y);

        RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverStatusOuterClass.RoverStatus.newBuilder();
        RoverStatusOuterClass.RoverStatus         status   = null;

        if (samData == null) {
            logger.error("sampleAnalysisData unavailable for sol = " + rover.getSpacecraftClock().getSol());
            status = rBuilder.setBatteryLevel(rover.getBattery()
                                                      .getPrimaryPowerUnits())
                    .setSolNumber(rover.getSpacecraftClock().getSol())
                    .setLocation(lBuilder.build()).setNotes("SAM Spectroscope unavailable!")
                    .setSCET(System.currentTimeMillis()).setModuleReporting(ModuleDirectory.Module.SAM_SPECTROMETER
                                                                                    .getValue())
                    .build();
        } else {
            status = rBuilder.setBatteryLevel(rover.getBattery()
                                                      .getPrimaryPowerUnits())
                    .setSolNumber(rover.getSpacecraftClock().getSol())
                    .setLocation(lBuilder.build()).setNotes("SAM Spectroscope engaged!")
                    .setModuleMessage(samData)
                    .setSCET(System.currentTimeMillis()).setModuleReporting(ModuleDirectory.Module.SAM_SPECTROMETER
                                                                                    .getValue())
                    .build();
        }
        rover.state = rover.transmittingState;
        rover.transmitMessage(status.toByteArray());
    }
}
