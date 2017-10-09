package space.exploration.mars.rover.diagnostics;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.InstructionPayloadOuterClass;
import space.exploration.mars.rover.communication.RoverStatusOuterClass;
import space.exploration.mars.rover.kernel.ModuleDirectory;
import space.exploration.mars.rover.kernel.Rover;
import space.exploration.mars.rover.utils.RoverUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RoverGarbageCollector {
    public static final int                      MAX_PENDING_MESSAGES = 15;
    private             Rover                    rover                = null;
    private             ScheduledExecutorService roverGC              = null;
    private             String                   errorMessage         = "";
    private             Logger                   logger               = LoggerFactory.getLogger
            (RoverGarbageCollector.class);

    public RoverGarbageCollector(Rover rover) {
        this.rover = rover;
        this.roverGC = Executors.newSingleThreadScheduledExecutor();
        roverGC.scheduleAtFixedRate(queueMonitor, 0l, 30l, TimeUnit.SECONDS);
    }

    public Runnable queueMonitor = new Runnable() {
        @Override
        public void run() {
            if (rover.getInstructionQueue().size() >= MAX_PENDING_MESSAGES
                    && (rover.getState() == rover.getListeningState())) {
                errorMessage = "Diagnostics module clearing instructionQueue because maxLength has been " +
                        "reached.";
                logger.error(errorMessage);

                errorMessage += " Number of messages lost = " + rover.getInstructionQueue().size();

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
                rover.setState(rover.getTransmittingState());
                rover.transmitMessage(getDistressSignal());
                return;
            } else if (rover.getState() != rover.getHibernatingState()) {
                if (!rover.getInstructionQueue().isEmpty()) {
                    logger.info("RoverGC trimming instructionQueue. Current length = " + rover.getInstructionQueue()
                            .size());
                }
                rover.processPendingMessageQueue();
            }
        }
    };

    private byte[] getDistressSignal() {
        RoverStatusOuterClass.RoverStatus.Builder rBuilder = RoverStatusOuterClass.RoverStatus.newBuilder();

        rBuilder.setModuleReporting(ModuleDirectory.Module.DIAGNOSTICS.getValue());
        rBuilder.setBatteryLevel(rover.getBattery().getPrimaryPowerUnits());
        rBuilder.setLocation(RoverUtil.getLocation(rover.getMarsArchitect().getRobot().getLocation()));
        rBuilder.setSCET(System.currentTimeMillis());
        rBuilder.setSolNumber(rover.getSol());
        rBuilder.setNotes("Houston, I am overworked and underpaid! " + errorMessage);
        return rBuilder.build().toByteArray();
    }
}
