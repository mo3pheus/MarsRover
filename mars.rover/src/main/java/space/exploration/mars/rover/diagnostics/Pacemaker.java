package space.exploration.mars.rover.diagnostics;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.InstructionPayloadOuterClass;
import space.exploration.mars.rover.communication.RoverStatusOuterClass;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.mars.rover.kernel.ModuleDirectory;
import space.exploration.mars.rover.kernel.Rover;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by sanketkorgaonkar on 5/15/17.
 */
public class Pacemaker {
    public static final int                           MAX_PENDING_MSGS      = 15;
    private             ScheduledExecutorService      scheduler             = null;
    private             Rover                         rover                 = null;
    private             int                           sleepAfterTimeSeconds = 0;
    private             HeartBeatOuterClass.HeartBeat heartBeat             = null;

    private Logger logger = LoggerFactory.getLogger(Pacemaker.class);

    public Pacemaker(Rover rover) {
        this.rover = rover;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        sleepAfterTimeSeconds = Integer.parseInt(rover.getMarsConfig().getProperty("mars.rover.sleepAfterTime" +
                                                                                           ".seconds"));
    }

    public void pulse() {
        Runnable heart = new Runnable() {
            @Override
            public void run() {
                logger.debug("Pacemaker performing its due diligence.");
                if (rover.getInstructionQueue().size() >= MAX_PENDING_MSGS) {
                    String errorMessage = "Diagnostics module clearing instructionQueue because maxLength has been " +
                            "reached.";
                    logger.error(errorMessage);

                    errorMessage += " Number of messages lost = " + MAX_PENDING_MSGS;

                    for (byte[] message : rover.getInstructionQueue()) {
                        try {
                            InstructionPayloadOuterClass.InstructionPayload instructionPayload =
                                    InstructionPayloadOuterClass.InstructionPayload.parseFrom(message);

                            errorMessage += instructionPayload.toString();
                        } catch (InvalidProtocolBufferException invalidProtocol) {
                            logger.error("Invalid protocol buffer for instructionPayload", invalidProtocol);
                            rover.writeErrorLog("Invalid protocol buffer for instructionPayload", invalidProtocol);
                        }
                    }

                    rover.writeErrorLog(errorMessage, null);
                    rover.getInstructionQueue().clear();
                    return;
                }

                if (rover.getState() == rover.getHibernatingState()) {
                    logger.error("Diagnostics inhibited because rover is in hibernating state.");
                    rover.writeErrorLog("Diagnostics inhibited because rover is in hibernating state.", null);
                    return;
                }

                if (rover.getState() == rover.getSleepingState()) {
                    if (awakenRover()) {
                        logger.info("Awakening rover from slumber");
                        rover.getMarsArchitect().getRobot().setColor(EnvironmentUtils.findColor(rover.getMarsConfig()
                                                                                                        .getProperty
                                                                                                                (EnvironmentUtils.ROBOT_COLOR)));
                        rover.getMarsArchitect().returnSurfaceToNormal();
                        rover.setState(rover.getListeningState());
                        rover.setTimeMessageReceived(System.currentTimeMillis());
                    } else {
                        logger.error("Diagnostics inhibited because rover is sleeping to conserve battery");
                        rover.writeErrorLog("Diagnostics inhibited because rover is sleeping to conserve battery",
                                            null);
                        return;
                    }
                }

                rover.processPendingMessageQueue();

                if (putRoverToSleep()) {
                    String sleepWarning = "Putting rover to sleep - since its been a while since an instruction was" +
                            " received.";
                    logger.info(sleepWarning);
                    rover.writeSystemLog(sleepWarning);
                    rover.setState(rover.getSleepingState());
                    rover.sleep();
                }

                if (rover.isDiagnosticFriendly()) {
                    rover.powerCheck(1);
                    RoverStatusOuterClass.RoverStatus roverStatus = generateDiagnosticStatus();
                    try {
                        heartBeat = HeartBeatOuterClass.HeartBeat.parseFrom(roverStatus.getModuleMessage()
                                                                                    .toByteArray());

                    } catch (InvalidProtocolBufferException ipe) {
                        logger.error(ipe.getMessage());
                    }

                    System.out.println(heartBeat);
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
        hBuilder.setSCET(System.currentTimeMillis());
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
        RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverStatusOuterClass.RoverStatus.newBuilder();
        rBuilder.setModuleReporting(ModuleDirectory.Module.DIAGNOSTICS.getValue());
        rBuilder.setModuleMessage(generateHeartBeat().toByteString());

        return rBuilder.build();
    }

    private boolean putRoverToSleep() {
        if ((System.currentTimeMillis() - rover.getTimeMessageReceived()) > TimeUnit.SECONDS.toMillis
                (sleepAfterTimeSeconds)) {
            return true;
        }
        return false;
    }

    private boolean awakenRover() {
        int maxSleepForMinutes = Integer.parseInt(rover.getMarsConfig().getProperty("mars.rover.sleepForMax.minutes"));
        if (rover.getState() == rover.getSleepingState()) {
            long timeInSleepMins = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - rover
                    .getTimeMessageReceived() - TimeUnit.SECONDS
                    .toMillis(sleepAfterTimeSeconds));

            if (timeInSleepMins > maxSleepForMinutes) {
                return true;
            }
        }
        return false;
    }
}
