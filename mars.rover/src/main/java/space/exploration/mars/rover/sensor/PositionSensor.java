package space.exploration.mars.rover.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.spice.MSLRelativePositions;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.mars.rover.kernel.Rover;
import space.exploration.spice.utilities.PositionUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Lives as long as the spacecraftClock lives - since there is a dependency between the two.
 */
public class PositionSensor implements IsEquipment {
    private Logger                   logger        = LoggerFactory.getLogger(PositionSensor.class);
    private PositionUtils            positionUtils = null;
    private ScheduledExecutorService sensorUpdate  = null;
    private Rover                    rover         = null;

    public PositionSensor(Rover rover) {
        this.rover = rover;
        this.positionUtils = new PositionUtils();
        this.sensorUpdate = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        Runnable positionUpdate = new Runnable() {
            @Override
            public void run() {
                positionUtils.setUtcTime(rover.getSpacecraftClock().getUTCTime());
            }
        };

        sensorUpdate.scheduleAtFixedRate(positionUpdate, 0l, 1, TimeUnit.SECONDS);
    }

    public MSLRelativePositions.MSLRelPositionsPacket getPositionsData() {
        MSLRelativePositions.MSLRelPositionsPacket         positionsPacket = positionUtils.getPositionPacket();
        MSLRelativePositions.MSLRelPositionsPacket.Builder mBuilder        = MSLRelativePositions
                .MSLRelPositionsPacket.newBuilder();
        mBuilder.setSol(rover.getSpacecraftClock().getSol());
        mBuilder.mergeFrom(positionsPacket);

        MSLRelativePositions.MSLRelPositionsPacket mslRelPositionsPacket = mBuilder.build();
        logger.info(mslRelPositionsPacket.toString());
        return mslRelPositionsPacket;
    }

    @Override
    public int getLifeSpan() {
        return rover.getSpacecraftClock().getLifeSpan();
    }

    @Override
    public String getEquipmentName() {
        return "Position Sensor";
    }

    @Override
    public boolean isEndOfLife() {
        return rover.getSpacecraftClock().isEndOfLife();
    }
}
