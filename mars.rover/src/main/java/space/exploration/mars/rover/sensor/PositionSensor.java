package space.exploration.mars.rover.sensor;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.spice.MSLRelativePositions;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.spice.utilities.PositionUtils;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static space.exploration.mars.rover.kernel.SpacecraftClock.*;


/**
 * Lives as long as the spacecraftClock lives - since there is a dependency between the two.
 */
public class PositionSensor implements IsEquipment {
    private Logger                   logger          = LoggerFactory.getLogger(PositionSensor.class);
    private PositionUtils            positionUtils   = null;
    private ScheduledExecutorService sensorUpdate    = null;
    private Properties               roverProperties = null;
    private DateTime                 internalClock   = null;
    private DateTimeFormatter        clockFormatter  = null;
    private String                   sclkStartTime   = null;
    private int                      timeScaleFactor = 0;
    private long                     timeElapsedMs   = 0l;
    private int                      sol             = 0;
    private long                     missionDuration = 0l;

    public PositionSensor(Properties roverProperties) {
        this.roverProperties = roverProperties;
        this.positionUtils = new PositionUtils();
        this.sensorUpdate = Executors.newSingleThreadScheduledExecutor();
        clockFormatter = DateTimeFormat.forPattern(roverProperties.getProperty(SCLK_FORMAT));
        sclkStartTime = roverProperties.getProperty(SCLK_START_TIME);
        timeScaleFactor = Integer.parseInt(roverProperties.getProperty(SCLK_TIME_SCALE_FACTOR));
        internalClock = clockFormatter.parseDateTime(sclkStartTime);
        internalClock.withZone(DateTimeZone.UTC);
        String missionDurationString = roverProperties.getProperty(SCLK_MISSION_DURATION);
        missionDuration = TimeUnit.DAYS.toMillis(365 * Integer.parseInt(missionDurationString));
    }

    public void start() {
        Runnable positionUpdate = new Runnable() {
            @Override
            public void run() {
                internalClock = new DateTime(internalClock.getMillis() + (timeScaleFactor * TimeUnit.SECONDS.toMillis
                        (1)));
                timeElapsedMs += (timeScaleFactor * TimeUnit.SECONDS.toMillis(1));
                positionUtils.setUtcTime(clockFormatter.print(internalClock));
            }
        };

        sensorUpdate.scheduleAtFixedRate(positionUpdate, 0l, 1, TimeUnit.SECONDS);
    }

    public MSLRelativePositions.MSLRelPositionsPacket getPositionsData() {
        MSLRelativePositions.MSLRelPositionsPacket positionsPacket = positionUtils.getPositionPacket();
        logger.info(positionsPacket.toString());
        return positionsPacket;
    }

    @Override
    public int getLifeSpan() {
        return (int) (missionDuration - timeElapsedMs);
    }

    @Override
    public String getEquipmentName() {
        return "Position Sensor";
    }

    @Override
    public boolean isEndOfLife() {
        return (timeElapsedMs > missionDuration);
    }

}
