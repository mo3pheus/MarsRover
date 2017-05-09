package space.exploration.mars.rover.animation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.sensor.Spectrometer;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SpectrometerAnimationEngine {
    public static final Integer      SPECTROMETER_DEPTH = new Integer(99);
    private             JFrame       marsSurface        = null;
    private             Properties   marsConfig         = null;
    private             Spectrometer spectrometer       = null;
    private             Cell         robot              = null;
    private             Logger       logger             = LoggerFactory.getLogger(SpectrometerAnimationEngine.class);

    public SpectrometerAnimationEngine(Properties marsConfig, Cell robot, Spectrometer spectrometer,
                                       JFrame marsSurface) {
        this.marsConfig = marsConfig;
        this.robot = robot;
        this.spectrometer = spectrometer;
        this.marsSurface = marsSurface;
    }

    public void activateSpectrometer() {
        if (marsConfig == null || robot == null || spectrometer == null || marsSurface == null) {
            logger.error("ScpectrometerAnimationEngine not set up correctly");
            logger.error("Need marsConfig,robot,spectrometer and marsSurface.");
            return;
        }

        int delayMs = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils
                .SPECTROMETER_ANIMATION_DELAY));
        JLayeredPane contentPane = AnimationUtil.getContent(marsConfig);
        contentPane.add(robot, Cell.ROBOT_DEPTH);
        for (Cell explorationCell : getExplorationCells()) {
            contentPane.add(explorationCell, SPECTROMETER_DEPTH);
            marsSurface.setContentPane(contentPane);
            marsSurface.setVisible(true);
            try {
                Thread.sleep(delayMs);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
            contentPane.remove(explorationCell);
        }

        AnimationUtil.getStaticEnvironment(marsConfig, robot, marsSurface);
    }

    private List<Cell> getExplorationCells() {
        List<Cell> explorationCells = new ArrayList<Cell>();

        for (Point temp : spectrometer.getExplorationPoints()) {
            Cell expCell = new Cell(marsConfig);
            expCell.setLocation(temp);
            expCell.setColor(
                    EnvironmentUtils.findColor(marsConfig.getProperty(EnvironmentUtils.SPECTROMETER_SCAN_COLOR)));
            explorationCells.add(expCell);
        }

        return explorationCells;
    }

}
