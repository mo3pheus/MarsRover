package space.exploration.mars.rover.kernel;

import com.google.protobuf.InvalidProtocolBufferException;
import com.yammer.metrics.core.Meter;
import communications.protocol.ModuleDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.InstructionPayloadOuterClass;
import space.exploration.communications.protocol.communication.RoverStatusOuterClass;
import space.exploration.communications.protocol.radar.RadarContactListOuterClass;
import space.exploration.communications.protocol.service.WeatherQueryOuterClass;
import space.exploration.communications.protocol.softwareUpdate.SwUpdatePackageOuterClass;
import space.exploration.kernel.diagnostics.LogRequest;
import space.exploration.mars.rover.animation.RadarAnimationEngine;
import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.environment.RadarContactCell;
import space.exploration.mars.rover.utils.RadialContact;
import space.exploration.mars.rover.utils.RoverUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by sanket on 5/30/17.
 */
public class RadarScanningState implements State {
    public static final int CONTACT_DIAMETER = 8;

    private Meter  requests = null;
    private Rover  rover    = null;
    private Logger logger   = LoggerFactory.getLogger(RadarScanningState.class);

    public RadarScanningState(Rover rover) {
        this.rover = rover;
        requests = this.rover.getMetrics().newMeter(RadarScanningState.class, getStateName(), "requests", TimeUnit
                .HOURS);
    }

    @Override
    public void activateCameraById(String camId) {

    }

    @Override
    public void requestLogs(LogRequest.LogRequestPacket logRequestPacket) {
    }

    @Override
    public void updateSoftware(SwUpdatePackageOuterClass.SwUpdatePackage swUpdatePackage) {
        logger.error("Can not update software in " + getStateName());
    }

    @Override
    public Meter getRequests() {
        return requests;
    }

    @Override
    public void synchronizeClocks(String utcTime) {
        logger.debug("Can not sync clocks in " + getStateName());
    }

    @Override
    public String getStateName() {
        return "RadarSensor Scanning State";
    }

    @Override
    public void receiveMessage(byte[] message) {
        rover.reflectRoverState();
        logger.debug("RadarSensor Module received message, adding to rover's instruction queue");
        rover.getInstructionQueue().add(message);
        try {
            RoverUtil.writeSystemLog(rover, InstructionPayloadOuterClass.InstructionPayload.parseFrom(message), rover
                    .getInstructionQueue().size());
        } catch (InvalidProtocolBufferException ipe) {
            RoverUtil.writeErrorLog(rover, "Invalid Protocol Buffer Exception", ipe);
        }
    }

    @Override
    public void shootNeutrons() {
    }

    @Override
    public void gracefulShutdown() {
        logger.error(" Can not perform gracefulShutdown while in " + getStateName());
    }

    @Override
    public void transmitMessage(byte[] message) {
        RoverUtil.roverSystemLog(logger, " Invalid action error. Can not transmit in current state.", "ERROR");
    }

    @Override
    public void exploreArea() {
        RoverUtil.roverSystemLog(logger, " Invalid action error. Can not explore in current state.", "ERROR");
    }

    @Override
    public void move(InstructionPayloadOuterClass.InstructionPayload.TargetPackage targetPackage) {
        logger.debug("Can not move in " + getStateName());
    }

    @Override
    public void hibernate() {
        RoverUtil.roverSystemLog(logger, " Invalid action error. Can not hibernate in current state.", "ERROR");
    }

    @Override
    public void senseWeather(WeatherQueryOuterClass.WeatherQuery weatherQuery) {
        RoverUtil.roverSystemLog(logger, " Invalid action error. Can not senseWeather in current state.", "ERROR");
    }

    @Override
    public void scanSurroundings() {
        RoverUtil.roverSystemLog(logger, " Invalid action error. Can not scanSurroundings in current state.", "ERROR");
    }


    @Override
    public void performRadarScan() {
        requests.mark();
        rover.reflectRoverState();
        logger.debug("Performing RadarSensor Scan, current position = " + rover.getMarsArchitect().getRobot().getLocation()
                .toString());
        MarsArchitect marsArchitect = rover.getMarsArchitect();

        RoverStatusOuterClass.RoverStatus.Location location = RoverStatusOuterClass.RoverStatus.Location
                .newBuilder()
                .setX(marsArchitect.getRobot().getLocation().x)
                .setY(marsArchitect.getRobot().getLocation().y).build();
        RoverStatusOuterClass.RoverStatus         status   = null;
        RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverStatusOuterClass.RoverStatus.newBuilder();

        RadarContactListOuterClass.RadarContactList.Builder rContactListBuilder = RadarContactListOuterClass
                .RadarContactList.newBuilder();
        for (RadialContact r : rover.getRadar().getRadialContacts()) {
            rContactListBuilder.addContacts(r.getPolarPoint());
        }
        rContactListBuilder.setScaleFactor(rover.getRadar().getScaleFactor());

        status = rBuilder.setBatteryLevel(rover.getBattery()
                                                  .getPrimaryPowerUnits())
                .setSolNumber(rover.getSpacecraftClock().getSol()).setLocation(location).setNotes("RadarSensor scan performed!")
                .setSCET(System
                                 .currentTimeMillis())
                .setModuleMessage(rContactListBuilder.build().toByteString())
                .setModuleReporting(ModuleDirectory.Module.RADAR.getValue()).build();

        renderRadarAnimation();
        rover.getMarsArchitect().returnSurfaceToNormal();
        rover.state = rover.transmittingState;
        rover.transmitMessage(status.toByteArray());
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

    private void renderRadarAnimation() {
        Properties                       marsConfig           = rover.getRoverConfig().getMarsConfig();
        RadarAnimationEngine             radarAnimationEngine = new RadarAnimationEngine(marsConfig);
        List<RadialContact>              temp                 = rover.getRadar().getRadialContacts();
        java.util.List<RadarContactCell> contacts             = new ArrayList<>();
        for (Point p : rover.getRadar().getRelativeRovers()) {
            contacts.add(new RadarContactCell(marsConfig, p, Color.green, CONTACT_DIAMETER));
        }
        radarAnimationEngine.setContacts(contacts);
        radarAnimationEngine.setRadialContacts(temp);
        radarAnimationEngine.renderLaserAnimation();
        radarAnimationEngine.destroy();
    }
}
