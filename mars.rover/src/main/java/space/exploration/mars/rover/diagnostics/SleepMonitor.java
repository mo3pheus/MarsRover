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
        sleepAfterTimeMinutes = Integer.parseInt(rover.getMarsConfig().getProperty("mars.rover.sleepAfterTime" +
                                                                                           ".minutes"));
        logger.info("SleepMonitor initialized and activated!");
    }

    public void hardInterrupt() {
        logger.info("SleepMonitor is shuttingDown.");
        runThread = false;
        monitor.shutdownNow();
    }

    private boolean isRoverSleepy() {
        return (((System.currentTimeMillis() - rover.getTimeMessageReceived()) > TimeUnit.MINUTES.toMillis
                (sleepAfterTimeMinutes)) && (rover.getState() == rover.getListeningState()));
    }

    private boolean isRoverRested() {
        if (rover.getState() == rover.getSleepingState()) {
            int maxSleepForMinutes = Integer.parseInt(rover.getMarsConfig().getProperty("mars.rover.sleepForMax" +
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
            if (runThread) {
                Thread.currentThread().setName("sleepMonitor");

                if (isRoverRested()) {
                    logger.info("Awakening rover from slumber");
                    rover.wakeUp();
                } else if (isRoverSleepy()) {
                    try {
                        rover.acquireAceessLock("sleepMonitor");
                        rover.setState(rover.getSleepingState());
                        rover.releaseAccessLock("sleepMonitor");
                    } catch (InterruptedException ie) {
                        logger.error("Exception while acquiring rover.accessLock mutex", ie);
                    }
                    rover.sleep();
                }
            }
        }
    }
}
