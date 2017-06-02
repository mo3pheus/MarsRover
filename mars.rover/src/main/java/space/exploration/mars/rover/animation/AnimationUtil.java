package space.exploration.mars.rover.animation;

import scala.concurrent.forkjoin.ThreadLocalRandom;
import space.exploration.mars.rover.environment.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class AnimationUtil {
    public static List<Point> generateRobotPositions(Point start, Point end, int stepSize) {
        List<Point> positions = new ArrayList<Point>();

        int numXIterations = Math.abs(start.x - end.x) / stepSize;
        int lastStepSize   = Math.abs(start.x - end.x) % stepSize;
        int startX         = start.x;
        int startY         = start.y;
        for (int i = 0; i < numXIterations; i++) {
            startX = (start.x > end.x) ? startX - stepSize : startX + stepSize;
            Point temp = new Point(startX, startY);
            positions.add(temp);
        }
        if (lastStepSize > 0) {
            startX = (start.x > end.x) ? startX - lastStepSize : startX + lastStepSize;
            Point temp = new Point(startX, startY);
            positions.add(temp);
        }

        int numYIterations = Math.abs(start.y - end.y) / stepSize;
        lastStepSize = Math.abs(start.y - end.y) % stepSize;
        for (int i = 0; i < numYIterations; i++) {
            startY = (start.y > end.y) ? startY - stepSize : startY + stepSize;
            Point temp = new Point(startX, startY);
            positions.add(temp);
        }
        if (lastStepSize > 0) {
            startY = (start.y > end.y) ? startY - lastStepSize : startY + lastStepSize;
            Point temp = new Point(startX, startY);
            positions.add(temp);
        }

        return positions;
    }

    public static void getStaticEnvironment(Properties marsConfig, Cell robot, JFrame marsSurface) {
        JLayeredPane contentPane = AnimationUtil.getContent(marsConfig);
        contentPane.add(robot, Cell.ROBOT_DEPTH);
        marsSurface.setContentPane(contentPane);
        marsSurface.setVisible(true);
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    public static Cell getStartLocation(Properties matrixConfig) {
        Cell start   = new Cell(matrixConfig);
        int  sourceX = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.ROBOT_START_LOCATION).split(",")[0]);
        int  sourceY = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.ROBOT_START_LOCATION).split(",")[1]);
        start.setLocation(new Point(sourceX, sourceY));
        start.setColor(EnvironmentUtils.findColor(matrixConfig.getProperty(EnvironmentUtils.START_CELL_COLOR)));
        return start;
    }

    public static JLayeredPane getContent(Properties marsRoverConfig) {
        JLayeredPane marsSurface = new JLayeredPane();
        marsSurface.setBackground(
                EnvironmentUtils.findColor(marsRoverConfig.getProperty(EnvironmentUtils.MARS_SURFACE_COLOR)));
        marsSurface.setOpaque(true);

        List<Component> content = new ArrayList<Component>();

        Grid        grid            = new Grid(marsRoverConfig);
        WallBuilder wallBuilder     = new WallBuilder(marsRoverConfig);
        Cell        destinationCell = new Cell(marsRoverConfig);

        content.add(grid);
        content.add(wallBuilder);
        content.add(destinationCell);

        Map<Point, RoverCell> oldRovers = EnvironmentUtils.setUpOldRovers(marsRoverConfig);
        for (Point p : oldRovers.keySet()) {
            content.add(oldRovers.get(p));
        }

        for (int i = 0; i < content.size(); i++) {
            marsSurface.add(content.get(i), new Integer(i));
        }

        return marsSurface;
    }

    @Deprecated
    public static List<Component> generateSurfaceCells(Properties marsConfig) {
        int frameHeight = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.FRAME_HEIGHT_PROPERTY));
        int frameWidth  = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.FRAME_WIDTH_PROPERTY));
        int cellWidth   = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));

        List<Component> surfaceCells = new ArrayList<Component>();

        for (int i = 0; i <= frameHeight; i += cellWidth) {
            for (int j = 0; j <= frameWidth; j += cellWidth) {
                Cell cell = new Cell(marsConfig);
                cell.setLocation(new Point(i, j));
                cell.setColor(getSurfaceColor(233, 243, 114, 124, 0, 1));
                surfaceCells.add(cell);
            }
        }
        return surfaceCells;
    }


    public static Color getSurfaceColor(int rMin, int rMax, int gMin, int gMax, int bMin, int bMax) {
        int r = ThreadLocalRandom.current().nextInt(rMin, rMax);
        int g = ThreadLocalRandom.current().nextInt(gMin, gMax);
        int b = ThreadLocalRandom.current().nextInt(bMin, bMax);
        return new Color(r, g, b);
    }

    public static void getRobot(Properties marsRoverConfig, Point position, Cell robot) {
        robot.setLocation(position);
        robot.setCellWidth(12);
        robot.setColor(EnvironmentUtils.findColor(marsRoverConfig.getProperty(EnvironmentUtils.ROBOT_COLOR)));
    }
}
