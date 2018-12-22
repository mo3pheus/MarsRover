package space.exploration.mars.rover.animation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.kernel.Rover;

import java.awt.*;

public class SleepBreather implements Runnable {
    private          Logger  logger    = LoggerFactory.getLogger(SleepBreather.class);
    private volatile Rover   rover     = null;
    private volatile boolean runThread = true;

    public SleepBreather(Rover rover) {
        this.rover = rover;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("sleepBreather");
        logger.info("Sleep breather started");
        Cell  robot      = rover.getMarsArchitect().getRobot();
        Color robotColor = EnvironmentUtils.findColor("robotSleepMode");
        robot.setColor(robotColor);
        robot.repaint();

        while (!robotColor.equals(Color.white) && runThread) {
            int r = ((robotColor.getRed() + 1) <= 255) ? robotColor.getRed() + 1 : robotColor.getRed();
            int g = ((robotColor.getGreen() + 1) <= 255) ? robotColor.getGreen() + 1 : robotColor.getGreen();
            int b = ((robotColor.getBlue() + 1) <= 255) ? robotColor.getBlue() + 1 : robotColor.getBlue();
            robotColor = new Color(r, g, b);
            robot.setColor(robotColor);
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                logger.info("Sleep animation interrupted.");
                logger.debug("Sleep animation interrupted.", e);
            }
            robot.repaint();
        }

        while (!robotColor.equals(EnvironmentUtils.findColor("robotSleepMode")) && runThread) {
            int r = ((robotColor.getRed() - 1) >= 40) ? robotColor.getRed() - 1 : robotColor.getRed();
            int g = ((robotColor.getGreen() - 1) >= 40) ? robotColor.getGreen() - 1 : robotColor.getGreen();
            int b = ((robotColor.getBlue() - 1) >= 40) ? robotColor.getBlue() - 1 : robotColor.getBlue();
            robotColor = new Color(r, g, b);
            robot.setColor(robotColor);
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                logger.info("Sleep animation interrupted.");
                logger.debug("Sleep animation interrupted.", e);
            }
            robot.repaint();
        }
        logger.debug("Sleep breather finished.");
    }

    public void hardInterrupt() {
        this.runThread = false;
    }
}
