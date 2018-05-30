package space.exploration.mars.rover.kernel;

import com.yammer.metrics.core.Gauge;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.spice.utilities.TimeUtils;

import java.util.Observable;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Note: Be very careful in adding log statements in this class. Chances are the clock will log once every
 * Second and overwhelm storage.
 * <p>
 * The sclk is only as valid as the spice.utilities library provides. Which in turn is driven by the MSL mission data
 * processed.
 * Please contact sanket.korgaonkar@gmail.com if you have trouble extending the duration of the spacecraft clock.
 */
public class SpacecraftClock extends Observable implements IsEquipment {
    public static final String SCLK_FORMAT            = "mars.rover.mission.clock.format";
    public static final String SCLK_START_TIME        = "mars.rover.mission.clock.start";
    public static final String SCLK_MISSION_DURATION  = "mars.rover.mission.duration.years";
    public static final String SCLK_TIME_SCALE_FACTOR = "mars.rover.clock.timeScaleFactor";

    private          Logger                   logger          = LoggerFactory.getLogger(SpacecraftClock
                                                                                                .class);
    private          ScheduledExecutorService clockCounter    = null;
    private          DateTimeFormatter        clockFormatter  = null;
    private          String                   sclkStartTime   = null;
    private          int                      timeScaleFactor = 0;
    private          long                     missionDuration = 0l;
    private          long                     timeElapsedMs   = 0l;
    private          int                      sol             = 0;
    private          TimeUtils                clockService    = null;
    private volatile boolean                  runThread       = true;
    private          Gauge<Long>              sclkGauge       = null;
    private          int                      previousSol     = 0;
    private          Rover                    rover           = null;

    private DateTime internalClock;
    private Clock    clock;

    public SpacecraftClock(Properties marsConfig) {
        clockFormatter = DateTimeFormat.forPattern(marsConfig.getProperty(SCLK_FORMAT));
        sclkStartTime = marsConfig.getProperty(SCLK_START_TIME);
        timeScaleFactor = Integer.parseInt(marsConfig.getProperty(SCLK_TIME_SCALE_FACTOR));
        internalClock = clockFormatter.parseDateTime(sclkStartTime);
        internalClock = internalClock.withZone(DateTimeZone.UTC);
        clockService = new TimeUtils();

        logger.info("Valid utc time ranges are dictated by clockService as follows:");
        logger.info(clockService.getApplicableTimeFrame());
        logger.info("Given UTC = " + sclkStartTime);
        logger.info("Please ensure that positionSensor and sclk are synchronized");

        clockCounter = Executors.newSingleThreadScheduledExecutor();
        clock = new Clock();

        String missionDurationString = marsConfig.getProperty(SCLK_MISSION_DURATION);
        missionDuration = TimeUnit.DAYS.toMillis(365 * Integer.parseInt(missionDurationString));
        logger.info("Curiosity internal spacecraft clock started at :: " + internalClock);
        logger.info("Mission duration is set to :: " + missionDurationString + " year(s).");
        logger.info("Mission duration = " + missionDuration);

        sclkGauge = new Gauge<Long>() {
            @Override
            public Long value() {
                return internalClock.getMillis();
            }
        };
    }

    protected void resetSpacecraftClock(String utcTime) {
        logger.info("UTCTime = " + utcTime);
        internalClock = clockFormatter.parseDateTime(utcTime);
        internalClock = internalClock.withZone(DateTimeZone.UTC);

        logger.info("Valid utc time ranges are dictated by clockService as follows:");
        logger.info(clockService.getApplicableTimeFrame());
        logger.info("Given UTC = " + utcTime);
        logger.info("Please ensure that positionSensor and sclk are synchronized");

        clockCounter.shutdown();
        clockCounter = Executors.newSingleThreadScheduledExecutor();
        start();
    }

    public void hardInterrupt() {
        logger.info("SpacecraftClock shuttingDown.");
        runThread = false;
        clockCounter.shutdownNow();
    }

    public void start() {
        clockCounter.scheduleAtFixedRate(clock, 0l, 1l, TimeUnit.SECONDS);
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

    @Override
    public long getRequestMetric() {
        return sclkGauge.value();
    }

    @Override
    public String getEquipmentLifeSpanProperty() {
        return "mars.rover.spacecraft.clock.lifespan";
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

    public void setRover(Rover rover) {
        this.rover = rover;
    }

    private class Clock implements Runnable {
        @Override
        public void run() {
            if (runThread) {
                Thread.currentThread().setName("spacecraftClock");

                if (timeElapsedMs > missionDuration) {
                    logger.error("Houston! Spacecraft clock has reached end of mission life.");
                    stopClock();
                }

                internalClock = new DateTime(internalClock.getMillis() + (timeScaleFactor * TimeUnit.SECONDS.toMillis
                        (1)));
                clockService.updateClock(clockFormatter.print(internalClock));
                timeElapsedMs += (timeScaleFactor * TimeUnit.SECONDS.toMillis(1));
                sol = clockService.getSol();

                if (sol != previousSol) {
                    previousSol = sol;
                    rover.updateSensors(sol);
                }
            }
        }
    }
}
