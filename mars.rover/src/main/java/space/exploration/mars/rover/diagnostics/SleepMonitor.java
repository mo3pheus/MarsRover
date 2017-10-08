package space.exploration.mars.rover.diagnostics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.kernel.Rover;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SleepMonitor {
    private Logger                   logger                = LoggerFactory.getLogger(SleepMonitor.class);
    private Rover                    rover                 = null;
    private ScheduledExecutorService monitor               = null;
    private long                     waitForMins           = 0l;
    private int                      sleepAfterTimeMinutes = 0;

    public SleepMonitor(Rover rover) {
        this.rover = rover;
        monitor = Executors.newSingleThreadScheduledExecutor();
        monitor.scheduleAtFixedRate(snooze, 0l, 1l, TimeUnit.MINUTES);
        waitForMins = Integer.parseInt(rover.getMarsConfig().getProperty("mars.rover.sleepAfterTime.minutes"));
        sleepAfterTimeMinutes = Integer.parseInt(rover.getMarsConfig().getProperty("mars.rover.sleepAfterTime" +
                                                                                           ".minutes"));
    }

    private Runnable snooze = new Runnable() {
        @Override
        public void run() {
            if (isRoverRested()) {
                logger.info("Awakening rover from slumber");
                rover.wakeUp();
            } else if (isRoverSleepy()) {
                rover.setState(rover.getSleepingState());
                rover.sleep();
            }
        }
    };

    private boolean isRoverSleepy() {
        return (((System.currentTimeMillis() - rover.getTimeMessageReceived()) > TimeUnit.MINUTES.toMillis(waitForMins))
                && (rover.getState() == rover.getListeningState()));
    }

    private boolean isRoverRested() {
        if (rover.getState() == rover.getSleepingState()) {
            int maxSleepForMinutes = Integer.parseInt(rover.getMarsConfig().getProperty("mars.rover.sleepForMax" +
                                                                                                ".minutes"));
            long timeInSleepMins = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - rover
                    .getTimeMessageReceived() - TimeUnit.MINUTES
                    .toMillis(sleepAfterTimeMinutes));

            if (timeInSleepMins > maxSleepForMinutes) {
                return true;
            }
        }
        return false;
    }
}
