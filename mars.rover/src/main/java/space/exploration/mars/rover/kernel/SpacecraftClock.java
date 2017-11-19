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
    private static final String SCLK_FORMAT           = "mars.rover.mission.clock.format";
    private static final String SCLK_START_TIME       = "mars.rover.mission.clock.start";
    private static final String SCLK_MISSION_DURATION = "mars.rover.mission.duration.years";
    private static final String SCLK_FILE_PATH        = "mars.rover.sclk.file";

    private Logger                   logger          = LoggerFactory.getLogger(SpacecraftClock
                                                                                       .class);
    private ScheduledExecutorService clockCounter    = null;
    private DateTime                 internalClock   = null;
    private DateTimeFormatter        clockFormatter  = null;
    private String                   sclkStartTime   = null;
    private long                     missionDuration = 0l;
    private long                     timeElapsedMs   = 0l;
    private String                   clockFilePath   = "";

    public SpacecraftClock(Properties marsConfig) {
        clockFormatter = DateTimeFormat.forPattern(marsConfig.getProperty(SCLK_FORMAT));
        sclkStartTime = marsConfig.getProperty(SCLK_START_TIME);
        internalClock = clockFormatter.parseDateTime(sclkStartTime);
        clockFilePath = marsConfig.getProperty(SCLK_FILE_PATH);

        clockCounter = Executors.newSingleThreadScheduledExecutor();

        String missionDurationString = marsConfig.getProperty(SCLK_MISSION_DURATION);
        missionDuration = TimeUnit.DAYS.toMillis(365 * Integer.parseInt(missionDurationString));
        logger.info("Curiosity internal spacecraft clock started at :: " + internalClock);
        logger.info("Mission duration is set to :: " + missionDuration + " year(s).");
    }

    public void start() {
        Runnable clock = new Runnable() {
            @Override
            public void run() {

                if (timeElapsedMs > missionDuration) {
                    logger.error("Houston! Spacecraft clock has reached end of mission life.");
                    clockCounter.shutdown();
                }

                internalClock = new DateTime(internalClock.getMillis() + 1);
                timeElapsedMs++;
            }
        };

        clockCounter.scheduleAtFixedRate(clock, 0l, 1l, TimeUnit.MILLISECONDS);
    }

    public synchronized DateTime getInternalClock() {
        return internalClock;
    }

    public synchronized String getSclkTime() {
        return TimeUtils.getSpacecraftTime(clockFilePath, clockFormatter.print(internalClock));
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
}
