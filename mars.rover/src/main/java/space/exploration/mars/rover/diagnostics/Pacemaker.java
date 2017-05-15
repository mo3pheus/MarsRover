package space.exploration.mars.rover.diagnostics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.kernel.Rover;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by sanketkorgaonkar on 5/15/17.
 */
public class Pacemaker {
    private ScheduledExecutorService scheduler = null;
    private Rover                    rover     = null;

    private Logger logger = LoggerFactory.getLogger(Pacemaker.class);

    public Pacemaker(int threadPoolCount, Rover rover) {
        this.rover = rover;
        this.scheduler = Executors.newScheduledThreadPool(threadPoolCount);
    }

    public void heartBeat() {
        Runnable heart = new Runnable() {
            @Override
            public void run() {
                System.out.println(" SCET = " + System.currentTimeMillis()
                        + " this is rover heartBeat."
                );
                logger.info(" SCET = " + System.currentTimeMillis()
                        + " this is rover heartBeat.");
            }
        };

        ScheduledFuture<?> trigger = scheduler.scheduleAtFixedRate(heart, 60, 60, TimeUnit.SECONDS);
    }
}
