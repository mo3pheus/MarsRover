package space.exploration.mars.rover.kernel;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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

    private Logger                   logger              = LoggerFactory.getLogger(SpacecraftClock
                                                                                           .class);
    private ScheduledExecutorService clockCounter        = null;
    private DateTime                 internalClock       = null;
    private DateTimeFormatter        clockFormatter      = null;
    private String                   sclkStartTime       = null;
    private TimeUtils                clockService        = null;
    private int                      timeScaleFactor     = 0;
    private long                     missionDuration     = 0l;
    private long                     timeElapsedMs       = 0l;
    private int                      sol                 = 0;
    private double                   ephemerisTime       = 0.0d;
    private String                   applicableTimeFrame = "";
    private String                   clockFilePath       = "";
    private String                   calendarTime        = "";

    public SpacecraftClock(Properties marsConfig) {
        clockFormatter = DateTimeFormat.forPattern(marsConfig.getProperty(SCLK_FORMAT));
        sclkStartTime = marsConfig.getProperty(SCLK_START_TIME);
        timeScaleFactor = Integer.parseInt(marsConfig.getProperty(SCLK_TIME_SCALE_FACTOR));
        internalClock = clockFormatter.parseDateTime(sclkStartTime);
        internalClock.withZone(DateTimeZone.UTC);
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
                clockService.updateClock(clockFormatter.print(internalClock));
                timeElapsedMs += timeScaleFactor;
                sol = clockService.getSol();
            }
        };

        clockCounter.scheduleAtFixedRate(clock, 0l, 1l, TimeUnit.MILLISECONDS);
    }

    public DateTime getInternalClock() {
        return internalClock;
    }

    public String getUTCTime() {
        return clockFormatter.print(internalClock.getMillis());
    }

    public String getSclkTime() {
        String sclkString = clockService.getSclkTime();
        logger.info("Internal time::" + internalClock + " sclk of::" + sclkString);
        return sclkString;
    }

    public int getSol() {
        return sol;
    }

    public String getSclkStartTime() {
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

    public int getTimeScaleFactor() {
        return timeScaleFactor;
    }

    public long getMissionDuration() {
        return missionDuration;
    }

    public long getTimeElapsedMs() {
        return timeElapsedMs;
    }

    public double getEphemerisTime() {
        return clockService.getEphemerisTime();
    }

    public String getApplicableTimeFrame() {
        return clockService.getApplicableTimeFrame();
    }

    public String getClockFilePath() {
        return clockService.getClockFile().toPath().toString();
    }

    public String getCalendarTime() {
        return clockService.getCalendarTime();
    }

    public String toString() {
        space.exploration.communications.protocol.spacecraftClock.SpacecraftClock.SclkPacket.Builder sBuilder = space
                .exploration.communications.protocol.spacecraftClock.SpacecraftClock.SclkPacket.newBuilder();
        sBuilder.setMissionDurationMS(getMissionDuration());
        sBuilder.setSclkValue(getSclkTime());
        sBuilder.setStartTime(getSclkStartTime());
        sBuilder.setTimeElapsedMs(getTimeElapsedMs());
        sBuilder.setTimeScaleFactor(getTimeScaleFactor());
        sBuilder.setUtcTime(getUTCTime());
        sBuilder.setClockFile(clockService.getClockFile().getPath());
        sBuilder.setApplicableTimeFrame(clockService.getApplicableTimeFrame());
        sBuilder.setEphemerisTime(clockService.getEphemerisTime());
        sBuilder.setCalendarTime(clockService.getCalendarTime());
        sBuilder.setSol(sol);

        return sBuilder.build().toString();
    }
}
