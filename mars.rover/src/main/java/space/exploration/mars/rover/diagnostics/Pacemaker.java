package space.exploration.mars.rover.diagnostics;

import com.google.protobuf.InvalidProtocolBufferException;
import communications.protocol.ModuleDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.communication.RoverStatusOuterClass;
import space.exploration.communications.protocol.diagnostics.HeartBeatOuterClass;
import space.exploration.communications.protocol.spice.MSLRelativePositions;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.mars.rover.kernel.Rover;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by sanketkorgaonkar on 5/15/17.
 */
public class Pacemaker {
    private ScheduledExecutorService      scheduler = null;
    private Rover                         rover     = null;
    private HeartBeatOuterClass.HeartBeat heartBeat = null;

    private Logger logger = LoggerFactory.getLogger(Pacemaker.class);

    public Pacemaker(Rover rover) {
        this.rover = rover;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void pulse() {
        Runnable heart = new Runnable() {
            @Override
            public void run() {
                if (rover.getState() == rover.getHibernatingState()) {
                    logger.error("Diagnostics inhibited because rover is in hibernating state."
                                         + " Current instructionQueue length = " + rover.getInstructionQueue().size());
                    rover.writeErrorLog("Diagnostics inhibited because rover is in hibernating state.", null);
                } else if (rover.getState() == rover.getSleepingState()) {
                    logger.info("Diagnostics inhibited because rover is sleeping to conserve battery"
                                        + " Current instructionQueue length = " + rover.getInstructionQueue()
                            .size());
                    rover.writeSystemLog("Diagnostics inhibited because rover is sleeping to conserve battery",
                                         rover.getInstructionQueue().size());
                } else if (rover.isDiagnosticFriendly()) {
                    rover.powerCheck(1);
                    RoverStatusOuterClass.RoverStatus roverStatus = generateDiagnosticStatus();
                    try {
                        heartBeat = HeartBeatOuterClass.HeartBeat.parseFrom(roverStatus.getModuleMessage()
                                                                                    .toByteArray());

                    } catch (InvalidProtocolBufferException ipe) {
                        logger.error(ipe.getMessage());
                    }

                    logger.debug(heartBeat.toString());
                    rover.setState(rover.getTransmittingState());
                    rover.transmitMessage(roverStatus.toByteArray());
                }
            }
        };

        int                pulse   = Integer.parseInt(rover.getMarsConfig().getProperty("mars.rover.heartbeat.pulse"));
        ScheduledFuture<?> trigger = scheduler.scheduleAtFixedRate(heart, 60, pulse, TimeUnit.SECONDS);
    }

    public void interrupt() {
        logger.error("Pacemaker diagnostic module interrupted.");
        rover.writeErrorLog("Pacemaker diagnostic module interrupted.", null);
        scheduler.shutdown();
    }

    private HeartBeatOuterClass.HeartBeat generateHeartBeat() {
        HeartBeatOuterClass.HeartBeat.Builder hBuilder = HeartBeatOuterClass.HeartBeat.newBuilder();
        hBuilder.setSCET(rover.getSpacecraftClock().getInternalClock().getMillis());
        hBuilder.setBatteryLevel(rover.getBattery().getPrimaryPowerUnits());
        hBuilder.setNotes("This is rover Curiosity. Sending HeartBeat!");
        hBuilder.setModuleReporting(ModuleDirectory.Module.DIAGNOSTICS.getValue());
        hBuilder.setSolNumber(rover.getSol());

        RoverStatusOuterClass.RoverStatus.Location location = RoverStatusOuterClass.RoverStatus.Location.newBuilder()
                .setX(rover.getMarsArchitect().getRobot().getLocation().x)
                .setY(rover.getMarsArchitect().getRobot().getLocation().y).build();

        hBuilder.setLocation(location);

        //Equipment check
        for (IsEquipment equipment : rover.getEquimentList()) {
            hBuilder.putEquipmentHealth(equipment.getEquipmentName(), equipment.getLifeSpan());
        }

        return hBuilder.build();
    }

    private RoverStatusOuterClass.RoverStatus generateDiagnosticStatus() {
        RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverStatusOuterClass.RoverStatus
                .newBuilder();
        MSLRelativePositions.MSLRelPositionsPacket mslRelPositionsPacket = rover.getPositionSensor().getPositionsData();
        rBuilder.setMslPositionsPacket(mslRelPositionsPacket);
        rBuilder.setModuleReporting(ModuleDirectory.Module.DIAGNOSTICS.getValue());
        rBuilder.setModuleMessage(generateHeartBeat().toByteString());

        if (mslRelPositionsPacket.getHgaPass()) {
            rBuilder.setNotes("HGA PASS DETECTED AT THIS TIME!");
        }

        RoverStatusOuterClass.RoverStatus roverStatus = rBuilder.build();

        return roverStatus;
    }
}
