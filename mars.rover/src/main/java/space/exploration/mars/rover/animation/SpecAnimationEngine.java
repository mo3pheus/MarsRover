package space.exploration.mars.rover.animation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.environment.EnvironmentUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class SpecAnimationEngine implements SpectrometerAnimation {
    public static final long    DELAY_MS           = TimeUnit.SECONDS.toMillis(3l);
    public static final Integer SPECTROMETER_DEPTH = new Integer(99);

    private Logger     logger      = LoggerFactory.getLogger(SpecAnimationEngine.class);
    private JFrame     marsSurface = null;
    private Cell       robot       = null;
    private Color      cellColor   = null;
    private Properties marsConfig  = null;
    private int        cellWidth   = 0;

    public SpecAnimationEngine(JFrame marsSurface, Cell robot, Properties marsConfig) {
        this.marsSurface = marsSurface;
        this.robot = robot;
        this.marsConfig = marsConfig;
        cellWidth = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));
    }

    @Override
    public void renderAnimation() {
        JLayeredPane contentPane = AnimationUtil.getContent(marsConfig);
        contentPane.add(robot, Cell.ROBOT_DEPTH);
        List<Point> explorationPoints = getExplorationLocations();
        for (Point explorationPoint : explorationPoints) {
            Cell tempCell = new Cell(marsConfig);
            tempCell.setColor(cellColor);
            tempCell.setLocation(explorationPoint);
            contentPane.add(tempCell, SPECTROMETER_DEPTH);
            marsSurface.setContentPane(contentPane);
            marsSurface.setVisible(true);
            try {
                Thread.sleep(DELAY_MS);
            } catch (Exception e) {
                logger.error("SpecAnimationEngine interrupted.", e);
            }
            contentPane.remove(tempCell);
        }

        AnimationUtil.getStaticEnvironment(marsConfig, robot, marsSurface);
    }

    @Override
    public void setColor(Color color) {
        this.cellColor = color;
    }

    private List<Point> getExplorationLocations() {
        List<Point> explorationPoints = new ArrayList<>();

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int x = robot.getLocation().x + (i * cellWidth);
                int y = (robot.getLocation().y + (j * cellWidth));
                if (x >= 0 && x < 700 && y >= 0 && y < 700) {
                    explorationPoints.add(new Point(x, y));
                }
            }
        }
        return explorationPoints;
    }
}
