package space.exploration.mars.rover.navigation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.animation.AnimationUtil;
import space.exploration.mars.rover.environment.EnvironmentUtils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NavigationEngine implements PerformsNavigation {
    private static final int                   MAX_PATH_LENGTH   = 1000;
    private static final long                  MAX_WAIT_TIME     = TimeUnit.SECONDS.toMillis(1);
    private              Map<Integer, NavCell> gridMap           = null;
    private              Properties            matrixConfig      = null;
    private              int                   cellWidth         = 0;
    private              int                   animationStepSize = 5;
    private              List<Point>           robotPath         = null;
    private              Logger                logger            = LoggerFactory.getLogger(NavigationEngine.class);

    public NavigationEngine(Properties matrixConfig) {
        this.matrixConfig = matrixConfig;
        this.gridMap = new HashMap<Integer, NavCell>();
        gridMap = NavUtil.populateGridMap(matrixConfig);
        this.cellWidth = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));
        this.animationStepSize = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.ANIMATION_STEP_SIZE));

        configureAdjacency();
    }

    public List<Point> getRobotPath() {
        return robotPath;
    }

    public List<Point> getAnimationCalibratedRobotPath() {
        List<Point> calibratedPath = new ArrayList<Point>();
        if (robotPath != null) {
            for (int i = 0; i < robotPath.size() - 1; i++) {
                Point       start      = robotPath.get(i);
                Point       end        = robotPath.get(i + 1);
                List<Point> tempPoints = AnimationUtil.generateRobotPositions(start, end, animationStepSize);
                calibratedPath.addAll(tempPoints);
            }
        }
        return calibratedPath;
    }

    /**
     * This method provides the navigation functionality to get from point start
     * to point end. The following code implements the A* algorith m.
     */
    public List<Point> navigate(NavCell start, NavCell end) {
        List<NavCell> open   = new ArrayList<NavCell>();
        List<NavCell> closed = new ArrayList<NavCell>();
        open.add(start);

        boolean done      = false;
        long    startTime = System.currentTimeMillis();
        while ((!done) && ((System.currentTimeMillis() - startTime) < MAX_WAIT_TIME)) {
            int     minFIndex = NavUtil.getMinFCell(open, start, end, cellWidth);
            NavCell current   = open.get(minFIndex);
            open.remove(minFIndex);
            closed.add(current);

            if (current.equals(end)) {
                System.out.println("Path found!");
                return calcPath(start, current);
            }

            List<NavCell> adjacentNodes = NavUtil.getAdjNodesFromGrid(gridMap, current.getAdjacentNodes());
            for (int i = 0; i < adjacentNodes.size(); i++) {
                NavCell cAdjNode = adjacentNodes.get(i);
                if (closed.contains(cAdjNode)) {
                    continue;
                }

                if (!open.contains(cAdjNode)) {
                    cAdjNode.setParent(current);
                    double gCost = NavUtil.getGCost(cAdjNode, start, cellWidth);
                    cAdjNode.setgCost(gCost);
                    int hCost = Math.abs(cAdjNode.getCenter().x - end.getCenter().x)
                            + Math.abs(cAdjNode.getCenter().y - end.getCenter().y);
                    cAdjNode.sethCost(hCost);
                    cAdjNode.setfCost(hCost + cAdjNode.getgCost());
                    open.add(cAdjNode);
                } else {
                    double newGCost = NavUtil.getGCost(current, start, cellWidth) + cellWidth;
                    if (cAdjNode.getgCost() > newGCost) {
                        cAdjNode.setgCost(newGCost);
                        cAdjNode.setfCost(cAdjNode.gethCost() + cAdjNode.getgCost());
                        cAdjNode.setParent(current);
                    }
                }
            }
            done = open.isEmpty();
        }

        if (open.isEmpty()) {
            logger.error(
                    "No path was found between start => " + start.toString() + " and end => " + end.toString());
            return null;
        }
        logger.error("NavigationEngine may have detected a possible cycle.");
        return null;
    }

    /**
     * @return the gridMap
     */
    public Map<Integer, NavCell> getGridMap() {
        return gridMap;
    }

    /**
     * @param gridMap the gridMap to set
     */
    public void setGridMap(Map<Integer, NavCell> gridMap) {
        this.gridMap = gridMap;
    }

    private void logPath() {
        for (Point p : this.robotPath) {
            logger.debug("navEnginePosn :: " + p.toString());
        }
    }

    private void configureAdjacency() {
        for (int id : gridMap.keySet()) {
            NavCell             nCell    = gridMap.get(id);
            AdjacencyCalculator adSensor = new AdjacencyCalculator(nCell.getCenter(), matrixConfig);
            nCell.setAdjacentNodes(adSensor.getAdjacentNodes());
        }
    }

    private List<Point> calcPath(NavCell start, NavCell current) {
        List<Point> path = new ArrayList<Point>();

        boolean done = true;
        while (done) {
            if (current.equals(start)) {
                done = false;
            }
            path.add(current.getCenter());
            current = current.getParent();
        }
        Collections.reverse(path);
        return path;
    }
}
