package space.exploration.mars.rover.kernel;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.spice.utilities.TimeUtils;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Note: Be very careful in adding log statements in this class. Chances are the clock will log once every
 * milliSecond and overwhelm storage.
 * <p>
 * The sclk is only as valid as the spice.utilities library provides. Which in turn is driven by the MSL mission data
 * processed.
 * Please contact sanket.korgaonkar@gmail.com if you have trouble extending the duration of the spacecraft clock.
 */
public class SpacecraftClock implements IsEquipment {
    private static final String SCLK_FORMAT            = "mars.rover.mission.clock.format";
    private static final String SCLK_START_TIME        = "mars.rover.mission.clock.start";
    private static final String SCLK_MISSION_DURATION  = "mars.rover.mission.duration.years";
    private static final String SCLK_TIME_SCALE_FACTOR = "mars.rover.clock.timeScaleFactor";

    private Logger                   logger          = LoggerFactory.getLogger(SpacecraftClock
                                                                                       .class);
    private ScheduledExecutorService clockCounter    = null;
    private DateTime                 internalClock   = null;
    private DateTimeFormatter        clockFormatter  = null;
    private String                   sclkStartTime   = null;
    private TimeUtils                clockService    = null;
    private int                      timeScaleFactor = 0;
    private long                     missionDuration = 0l;
    private long                     timeElapsedMs   = 0l;

    public SpacecraftClock(Properties marsConfig) {
        clockFormatter = DateTimeFormat.forPattern(marsConfig.getProperty(SCLK_FORMAT));
        sclkStartTime = marsConfig.getProperty(SCLK_START_TIME);
        timeScaleFactor = Integer.parseInt(marsConfig.getProperty(SCLK_TIME_SCALE_FACTOR));
        internalClock = clockFormatter.parseDateTime(sclkStartTime);
        clockService = new TimeUtils();

        clockCounter = Executors.newSingleThreadScheduledExecutor();

        String missionDurationString = marsConfig.getProperty(SCLK_MISSION_DURATION);
        missionDuration = TimeUnit.DAYS.toMillis(365 * Integer.parseInt(missionDurationString));
        logger.info("Curiosity internal spacecraft clock started at :: " + internalClock);
        logger.info("Mission duration is set to :: " + missionDurationString + " year(s).");
        logger.info("Mission duration = " + missionDuration);
    }

    public void start() {
        Runnable clock = new Runnable() {
            @Override
            public void run() {
                if (timeElapsedMs > missionDuration) {
                    logger.error("Houston! Spacecraft clock has reached end of mission life.");
                    stopClock();
                }

                internalClock = new DateTime(internalClock.getMillis() + timeScaleFactor);
                timeElapsedMs += timeScaleFactor;
            }
        };

        clockCounter.scheduleAtFixedRate(clock, 0l, 1l, TimeUnit.MILLISECONDS);
    }

    public synchronized DateTime getInternalClock() {
        return internalClock;
    }

    public synchronized String getSclkTime() {
        String sclkString = clockService.getSpacecraftTime(clockFormatter.print(internalClock));
        logger.info("Internal time::" + internalClock + " sclk of::" + sclkString);
        return sclkString;
    }

    public synchronized String getSclkStartTime() {
        return sclkStartTime;
    }

    @Override
    public int getLifeSpan() {
        return (int) (missionDuration - timeElapsedMs);
    }

    @Override
    public String getEquipmentName() {
        return "Spacecraft Clock -> " + getSclkTime();
    }

    @Override
    public boolean isEndOfLife() {
        return (timeElapsedMs > missionDuration);
    }

    public void stopClock() {
        clockCounter.shutdown();
    }
}
