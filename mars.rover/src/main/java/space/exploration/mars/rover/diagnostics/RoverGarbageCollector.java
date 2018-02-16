package space.exploration.mars.rover.diagnostics;

import communications.protocol.ModuleDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.communication.RoverStatusOuterClass;
import space.exploration.mars.rover.kernel.Rover;
import space.exploration.mars.rover.utils.RoverUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RoverGarbageCollector {
    public static final int   MAX_PENDING_MESSAGES = 15;
    private volatile    Rover rover                = null;

    private          ScheduledExecutorService roverGC      = null;
    private          String                   errorMessage = "";
    private          Logger                   logger       = LoggerFactory.getLogger
            (RoverGarbageCollector.class);
    private          QueueMonitor             queueMonitor = null;
    private volatile boolean                  runThread    = true;

    public class QueueMonitor implements Runnable {
        @Override
        public void run() {
            if (runThread) {
                Thread.currentThread().setName("roverGarbageCollector");

                if (rover.getInstructionQueue().size() >= MAX_PENDING_MESSAGES
                        && (rover.getState() == rover.getListeningState())) {
                    errorMessage = "Diagnostics module clearing instructionQueue because maxLength has been " +
                            "reached.";
                    logger.error(errorMessage);

                    errorMessage += " Number of messages lost = " + rover.getInstructionQueue().size() + " :: " +
                            RoverUtil.getInstructionQueue(rover);

                    try {
                        byte[] distressSignal = getDistressSignal();
                        RoverUtil.writeErrorLog(rover, errorMessage, null);
                        rover.getInstructionQueue().clear();

                        if (rover.isGracefulShutdown()) {
                            logger.info("RoverGC detected a graceful shutdown signal in instructionQueue. Will honor " +
                                                "gracefulShutdown.");
                            rover.getInstructionQueue().add(RoverUtil.getGracefulShutdownCommand().toByteArray());
                        }

                        rover.setState(rover.getTransmittingState());
                        rover.transmitMessage(distressSignal);
                    } catch (Exception e) {
                        logger.error("Encountered error while generating distressSignal.", e);
                        rover.getInstructionQueue().clear();
                        rover.setState(rover.getListeningState());
                    }
                } else if (rover.getState() != rover.getHibernatingState()) {
                    if (!rover.getInstructionQueue().isEmpty()) {
                        logger.info("RoverGC trimming instructionQueue. Current length = " + rover.getInstructionQueue()
                                .size());
                        try {
                            rover.processPendingMessageQueue();
                        } catch (Exception e) {
                            logger.error("Encountered exception while processing instructionQueue", e);
                            rover.setState(rover.getListeningState());
                        }
                    }
                }
            }
        }
    }

    public RoverGarbageCollector(Rover rover) {
        this.rover = rover;
        this.roverGC = Executors.newSingleThreadScheduledExecutor();
        queueMonitor = new QueueMonitor();
    }

    public void start() {
        logger.info("Starting garbageCollection");
        roverGC.scheduleAtFixedRate(queueMonitor, 0L, 10L, TimeUnit.SECONDS);
    }

    public void interrupt() {
        logger.info("RoverGC interrupted");
        roverGC.shutdown();
    }

    public void hardInterrupt() {
        logger.info("RoverGC shuttingDown.");
        runThread = false;
        roverGC.shutdownNow();
    }

    private byte[] getDistressSignal() {
        RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverStatusOuterClass.RoverStatus.newBuilder();

        rBuilder.setModuleReporting(ModuleDirectory.Module.DIAGNOSTICS.getValue());
        rBuilder.setBatteryLevel(rover.getBattery().getPrimaryPowerUnits());
        rBuilder.setLocation(RoverUtil.getLocation(rover.getMarsArchitect().getRobot().getLocation()));
        rBuilder.setSCET(System.currentTimeMillis());
        rBuilder.setSolNumber(rover.getSpacecraftClock().getSol());
        rBuilder.setMslPositionsPacket(rover.getPositionSensor().getPositionsData());
        rBuilder.setNotes("Houston, I am overworked and underpaid! " + errorMessage);
        return rBuilder.build().toByteArray();
    }
}
