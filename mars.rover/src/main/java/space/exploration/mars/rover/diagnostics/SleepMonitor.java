package space.exploration.mars.rover.diagnostics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.kernel.Rover;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SleepMonitor {
    private volatile Rover                    rover                 = null;
    private volatile boolean                  runThread             = true;
    private          Logger                   logger                = LoggerFactory.getLogger(SleepMonitor.class);
    private          ScheduledExecutorService monitor               = null;
    private          Snooze                   snooze                = null;
    private          int                      sleepAfterTimeMinutes = 0;

    public SleepMonitor(Rover rover) {
        this.rover = rover;
        monitor = Executors.newSingleThreadScheduledExecutor();
        snooze = new Snooze();
        monitor.scheduleAtFixedRate(snooze, 0l, 1l, TimeUnit.MINUTES);
        sleepAfterTimeMinutes = Integer
                .parseInt(rover.getRoverConfig().getMarsConfig().getProperty("mars.rover.sleepAfterTime" +
                                                                                     ".minutes"));
        logger.info("SleepMonitor initialized and activated!");
    }

    public void hardInterrupt() {
        logger.info("SleepMonitor is shuttingDown.");
        runThread = false;
        monitor.shutdownNow();
    }

    private boolean isRoverSleepy() {
        logger.info("Rover received last message at :: " + rover.getTimeMessageReceived());
        logger.info("Time elapsed since last message = " + ((System.currentTimeMillis() - rover
                .getTimeMessageReceived()) + " milliseconds."));
        return (((System.currentTimeMillis() - rover.getTimeMessageReceived()) > TimeUnit.MINUTES.toMillis
                (sleepAfterTimeMinutes)));
    }

    private boolean isRoverRested() {
        if (rover.getState() == rover.getSleepingState()) {
            int maxSleepForMinutes = Integer
                    .parseInt(rover.getRoverConfig().getMarsConfig().getProperty("mars.rover.sleepForMax" +
                                                                                         ".minutes"));
            long timeInSleepMins = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - rover
                    .getTimeMessageReceived());

            return timeInSleepMins > maxSleepForMinutes;
        }
        return false;
    }

    public class Snooze implements Runnable {
        @Override
        public void run() {
            Thread.currentThread().setName("sleepMonitor");
            logger.info("RunThread = " + runThread);
            if (runThread) {
                if (isRoverRested()) {
                    rover.setState(rover.getSleepingState());
                    logger.info("Awakening rover from slumber");
                    rover.wakeUp();
                } else if (isRoverSleepy()) {
                    rover.setState(rover.getSleepingState());
                    logger.info("Rover is sleepy");
                    rover.sleep();
                }
            }
        }
    }
}
