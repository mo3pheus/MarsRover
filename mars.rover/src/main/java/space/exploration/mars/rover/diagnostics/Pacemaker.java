package space.exploration.mars.rover.diagnostics;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.communication.RoverStatusOuterClass;
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
    private ScheduledExecutorService      scheduler = null;
    private Rover                         rover     = null;
    private HeartBeatOuterClass.HeartBeat heartBeat = null;

    private Logger logger = LoggerFactory.getLogger(Pacemaker.class);

    public Pacemaker(int threadPoolCount, Rover rover) {
        this.rover = rover;
        this.scheduler = Executors.newScheduledThreadPool(threadPoolCount);
    }

    public void pulse() {
        Runnable heart = new Runnable() {
            @Override
            public void run() {
                if (rover.isDiagnosticFriendly()) {
                    RoverStatusOuterClass.RoverStatus roverStatus = generateDiagnosticStatus();
                    try {
                        heartBeat = HeartBeatOuterClass.HeartBeat.parseFrom(roverStatus.getModuleMessage()
                                .toByteArray());

                    } catch (InvalidProtocolBufferException ipe) {
                        logger.error(ipe.getMessage());
                    }

                    System.out.println(heartBeat);
                    logger.info(heartBeat.toString());
                    rover.getRadio().sendMessage(roverStatus.toByteArray());
                    rover.powerCheck(1);
                }
            }
        };

        int                pulse   = Integer.parseInt(rover.getMarsConfig().getProperty("mars.rover.heartbeat.pulse"));
        ScheduledFuture<?> trigger = scheduler.scheduleAtFixedRate(heart, pulse, 60, TimeUnit.SECONDS);
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
}
