package space.exploration.mars.rover.diagnostics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.kernel.Rover;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LauncherMonitor {
    private volatile Rover rover = null;

    private          ScheduledExecutorService roverLauncherMonitor = null;
    private          Logger                   logger               = LoggerFactory.getLogger
            (LauncherMonitor.class);
    private volatile boolean                  runThread            = true;

    public class LaunchProcessMonitor implements Runnable {
        @Override
        public void run() {
            Thread.currentThread().setName("launchProcessMonitor");
            if (rover.getLauncherProcess().isAlive() && runThread) {
                InputStream inputStream = rover.getLauncherProcess().getInputStream();
                try {
                    if (inputStream.available() > 0) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        String         line           = null;
                        while ((line = bufferedReader.readLine()) != null) {
                            logger.info(line);
                        }
                    }
                } catch (IOException e) {
                    logger.error("Error while obtaining launcher process input stream.");
                }
            }
        }
    }

    public synchronized void setRunThread(boolean runThread) {
        this.runThread = runThread;
    }

    public LauncherMonitor(Rover rover) {
        this.rover = rover;
        roverLauncherMonitor = Executors.newSingleThreadScheduledExecutor();
        roverLauncherMonitor.scheduleAtFixedRate(new LaunchProcessMonitor(), 0l, 500l, TimeUnit.MILLISECONDS);
    }
}
