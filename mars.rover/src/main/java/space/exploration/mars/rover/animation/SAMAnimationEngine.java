package space.exploration.mars.rover.animation;

import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.environment.EnvironmentUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SAMAnimationEngine {



    private JFrame marsSurface = null;
    private Cell robot = null;
    private Color cellColor = null;
    private Properties marsConfig = null;
    private int cellWidth = 0;

    public SAMAnimationEngine(JFrame marsSurface, Cell robot, Properties marsConfig) {
        this.marsSurface = marsSurface;
        this.robot = robot;
        this.marsConfig = marsConfig;
        cellWidth = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));
        cellColor = Color.green;
    }

    public void renderAnimation() {
        JLayeredPane contentPane = AnimationUtil.getContent(marsConfig);
        contentPane.add(robot, Cell.ROBOT_DEPTH);
        List<Point> explorationPoints = getExplorationLocations();
        Cell tempCell = new Cell(marsConfig);
        int c = 0;
        for (Point explorationPoint: explorationPoints) {
            tempCell.setColor(c % 2 == 0 ? Color.green : Color.blue);
            c++;
            tempCell.setLocation(explorationPoint);
            contentPane.add(tempCell, new Integer(59));
            marsSurface.setContentPane(contentPane);
            marsSurface.setVisible(true);
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            contentPane.remove(tempCell);
        }
        AnimationUtil.getStaticEnvironment(marsConfig, robot, marsSurface);
    }

    public void setColor(Color color) { this.cellColor = color; }

    private boolean isValid(int x, int y) {
        return (x >= 0 && x < 700) && (y >= 0 && y < 700);
    }

    private List<Point> getExplorationLocations() {
        List<Point> explorationPoints = new ArrayList<>();

        for (int i = -1; i <= 1; i++) {
            for(int j = -1; j <= 1; j++) {
                int x = robot.getLocation().x;
                int y = robot.getLocation().y;
                if (isValid(x, y)) {
                    explorationPoints.add(new Point(x - robot.getCellWidth()/2, y - robot.getCellWidth()/2));
                }
            }
        }
        return explorationPoints;
    }

}
