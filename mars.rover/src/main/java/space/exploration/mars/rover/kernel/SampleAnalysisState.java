package space.exploration.mars.rover.kernel;

import com.yammer.metrics.core.Meter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.InstructionPayloadOuterClass;
import space.exploration.communications.protocol.service.SamQueryOuterClass;
import space.exploration.communications.protocol.service.WeatherQueryOuterClass;
import space.exploration.communications.protocol.softwareUpdate.SwUpdatePackageOuterClass;
import space.exploration.kernel.diagnostics.LogRequest;

public class SampleAnalysisState implements State {
    private Rover rover = null;
    Logger logger = LoggerFactory.getLogger(SampleAnalysisState.class);

    public SampleAnalysisState(Rover rover) {
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
        return null;
    }

    @Override
    public void synchronizeClocks(String utcTime) {

    }

    @Override
    public void gracefulShutdown() {

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

    }

    @Override
    public void requestLogs(LogRequest.LogRequestPacket logRequestPacket) {

    }

    @Override
    public void sampleAnalysis(SamQueryOuterClass.SamQuery samQuery) {
        logger.info("Sample Analysis requested from MSL for sol = " + rover.getSpacecraftClock().getSol() + " " +
                            "samQuery = " + samQuery.toString());
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

        byte[] samData = rover.getSamSensor().getSamData();
        if (samData == null) {
            logger.error("sampleAnalysisData unavailable for sol = " + rover.getSpacecraftClock().getSol());
            rover.state = rover.listeningState;
            return;
        } else {
            rover.state = rover.transmittingState;
            rover.transmitMessage(samData);
        }
    }
}
