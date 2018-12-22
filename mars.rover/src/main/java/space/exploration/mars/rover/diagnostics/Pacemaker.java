package space.exploration.mars.rover.diagnostics;

import communications.protocol.ModuleDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.communication.RoverStatusOuterClass;
import space.exploration.communications.protocol.diagnostics.HeartBeatOuterClass;
import space.exploration.communications.protocol.kernel.EquipmentHealthOuterClass;
import space.exploration.communications.protocol.softwareUpdate.SwUpdatePackageOuterClass;
import space.exploration.communications.protocol.spice.MSLRelativePositions;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.mars.rover.kernel.Rover;
import space.exploration.mars.rover.utils.PositionPredictor;
import space.exploration.mars.rover.utils.RoverUtil;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by sanketkorgaonkar on 5/15/17.
 */
public class Pacemaker {
    private volatile Rover                    rover;
    private          ScheduledExecutorService scheduler;
    private          Pulse                    pulse;

    private volatile boolean runThread = true;

    private HeartBeatOuterClass.HeartBeat heartBeat = null;

    private Logger logger = LoggerFactory.getLogger(Pacemaker.class);

    public Pacemaker(Rover rover) {
        this.rover = rover;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        pulse = new Pulse();
        logger.info("Pacemaker initialized");
    }

    public void sendHeartBeat(boolean shutdown) {
        rover.powerCheck(1);
        RoverStatusOuterClass.RoverStatus roverStatus = null;
        try {
            roverStatus = generateDiagnosticStatus(shutdown);
            heartBeat = HeartBeatOuterClass.HeartBeat.parseFrom(roverStatus.getModuleMessage()
                                                                        .toByteArray());
            logger.debug(heartBeat.toString());
            rover.setState(rover.getTransmittingState());
            rover.transmitMessage(roverStatus.toByteArray());
        } catch (Exception e) {
            logger.error("Encountered exception while generating diagnostic heartBeat. Its possible this " +
                                 "instance is in a coverage gap for positionVectors.", e);
            rover.setState(rover.getListeningState());
        }
    }

    public void pulse() {
        int pulseInterval = Integer.parseInt(rover.getRoverConfig().getMarsConfig().getProperty("mars.rover.heartbeat" +
                                                                                                        ".pulse"));
        ScheduledFuture<?> trigger = scheduler.scheduleAtFixedRate(pulse, 60, pulseInterval, TimeUnit.SECONDS);
    }

    public void interrupt() {
        logger.info("Pacemaker diagnostic module interrupted.");
        scheduler.shutdown();
    }

    public void hardInterrupt() {
        logger.info("Pacemaker diagnostic module shuttingDown.");
        runThread = false;
        scheduler.shutdownNow();
    }

    public HeartBeatOuterClass.HeartBeat generateHeartBeat() {
        HeartBeatOuterClass.HeartBeat.Builder hBuilder = HeartBeatOuterClass.HeartBeat.newBuilder();
        hBuilder.setSCET(rover.getSpacecraftClock().getInternalClock().getMillis());
        hBuilder.setBatteryLevel(rover.getBattery().getPrimaryPowerUnits());
        hBuilder.setNotes("This is rover Curiosity. Sending HeartBeat! Rover software version = " + rover
                .getRoverConfig().getSoftwareVersion() + " Current process pid = " + ManagementFactory
                .getRuntimeMXBean().getName());
        hBuilder.setModuleReporting(ModuleDirectory.Module.DIAGNOSTICS.getValue());
        hBuilder.setSolNumber(rover.getSpacecraftClock().getSol());

        RoverStatusOuterClass.RoverStatus.Location location = RoverStatusOuterClass.RoverStatus.Location.newBuilder()
                .setX(rover.getMarsArchitect().getRobot().getLocation().x)
                .setY(rover.getMarsArchitect().getRobot().getLocation().y).build();

        hBuilder.setLocation(location);

        //Equipment check
        for (IsEquipment equipment : rover.getEquimentList()) {
            EquipmentHealthOuterClass.EquipmentHealth.Builder eBuilder = EquipmentHealthOuterClass.EquipmentHealth
                    .newBuilder();
            eBuilder.setEquipmentName(equipment.getEquipmentName());
            eBuilder.setLifeSpan(equipment.getLifeSpan());
            eBuilder.setRequestCount(equipment.getRequestMetric());
            hBuilder.addEquipmentHealth(eBuilder.build());
        }

        return hBuilder.build();
    }

    private RoverStatusOuterClass.RoverStatus generateDiagnosticStatus(boolean shutdown) {
        RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverStatusOuterClass.RoverStatus
                .newBuilder();

        MSLRelativePositions.MSLRelPositionsPacket mslRelPositionsPacket;
        try {
            mslRelPositionsPacket = rover.getPositionSensor().getPositionsData();
        } catch (Exception e) {
            logger.info("Coverage gap encountered at " + rover.getSpacecraftClock()
                    .getUTCTime() + " using predicted position instead.");
            PositionPredictor positionPredictor = new PositionPredictor(rover.getRoverConfig().getMarsConfig());
            mslRelPositionsPacket = positionPredictor
                    .getPredictedPosition(rover.getSpacecraftClock().getUTCTimestamp());
        }

        rBuilder.setMslPositionsPacket(mslRelPositionsPacket);
        rBuilder.setModuleReporting(ModuleDirectory.Module.DIAGNOSTICS.getValue());
        rBuilder.setModuleMessage(generateHeartBeat().toByteString());

        if (shutdown) {
            String notes = "Messages Lost = " + RoverUtil.getInstructionQueue(rover);
            rBuilder.setNotes(notes + "\nHouston - be advised Curiosity is shutting down. This will be the last " +
                                      "diagnostic " +
                                      "message. Farewell!");
        } else if (mslRelPositionsPacket.getHgaPass()) {
            rBuilder.setNotes("HGA PASS DETECTED AT THIS TIME!");
        }

        return rBuilder.build();
    }

    private class Pulse implements Runnable {
        @Override
        public void run() {
            Thread.currentThread().setName("pacemaker");
            logger.info("Rover current state = " + rover.getState().getStateName() + " runThread = " + runThread);
            if (runThread) {
                if (rover.getState() == rover.getHibernatingState()) {
                    logger.error("Diagnostics inhibited because rover is in hibernating state."
                                         + " Current instructionQueue length = " + rover.getInstructionQueue().size());
                    RoverUtil.writeErrorLog(rover, "Diagnostics inhibited because rover is in hibernating state.",
                                            null);
                } else if (rover.getState() == rover.getSleepingState()) {
                    logger.info("Diagnostics inhibited because rover is sleeping to conserve battery"
                                        + " Current instructionQueue length = " + rover.getInstructionQueue()
                            .size());
                    RoverUtil.writeSystemLog(rover, "Diagnostics inhibited because rover is sleeping to conserve " +
                                                     "battery",
                                             rover.getInstructionQueue().size());
                } else if (rover.isDiagnosticFriendly()) {
                    sendHeartBeat(false);
                }
            }
        }
    }
}
