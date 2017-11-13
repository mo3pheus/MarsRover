package space.exploration.mars.rover.navigation;

import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.environment.Wall;
import space.exploration.mars.rover.environment.WallBuilder;
import space.exploration.mars.rover.learning.RCell;
import space.exploration.mars.rover.learning.ReinforcementLearner;
import space.exploration.mars.rover.learning.ReinforcementLearnerUtil;
import space.exploration.mars.rover.navigation.NavCell.Direction;

import java.awt.*;
import java.util.Map;
import java.util.Properties;

public class AdjacencyCalculator {
    private static final int                   VALID_DIR      = 4;
    private              int                   cellWidth      = -1;
    private              int                   frameHeight    = -1;
    private              int                   frameWidth     = -1;
    private              Point[]               adjNodes       = new Point[VALID_DIR];
    private              NavCell[]             adjacentNodes  = new NavCell[VALID_DIR];
    private              RCell[]               adjacentRNodes = new RCell[VALID_DIR];
    private              Map<Integer, NavCell> gridMap        = null;
    private              RCell[][]             rGridMap       = null;
    private              WallBuilder           wallBuilder    = null;
    private              int                   lidarCost      = 0;

    private double rlMinReward = 0.0d;
    private double rlMaxReward = 0.0d;

    public AdjacencyCalculator(Point center, Properties marsConfig) {
        this.gridMap = NavUtil.populateGridMap(marsConfig);
        this.wallBuilder = new WallBuilder(marsConfig);

        cellWidth = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));
        frameHeight = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.FRAME_HEIGHT_PROPERTY));
        frameWidth = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.FRAME_WIDTH_PROPERTY));

        adjNodes[Direction.NORTH.getValue()] = new Point(center.x, center.y - cellWidth);
        adjNodes[Direction.SOUTH.getValue()] = new Point(center.x, center.y + cellWidth);
        adjNodes[Direction.EAST.getValue()] = new Point(center.x + cellWidth, center.y);
        adjNodes[Direction.WEST.getValue()] = new Point(center.x - cellWidth, center.y);

        rlMaxReward = Double.parseDouble(marsConfig.getProperty(ReinforcementLearner.RL_ENGINE_PREFIX + ".maxReward"));
        rlMinReward = Double.parseDouble(marsConfig.getProperty(ReinforcementLearner.RL_ENGINE_PREFIX + ".minReward"));
    }

    public void setrGridMap(RCell[][] rGridMap) {
        this.rGridMap = rGridMap;
    }

    /**
     * Returns a valid NavCell for viable navigation and null for invalid
     * navigation. For the order of the returned array please refer to
     * NavCell.Direction
     *
     * @return Array of adjacentNodes
     */
    public NavCell[] getAdjacentNodes() {
        for (int i = 0; i < VALID_DIR; i++) {
            Point temp = adjNodes[i];
            if (temp.x < 0 || temp.x >= frameWidth || temp.y < 0 || temp.y >= frameHeight || intersectsWall(temp)) {
                adjacentNodes[i] = null;
                continue;
            } else {
                int     id    = NavUtil.findNavId(gridMap, temp);
                NavCell nCell = gridMap.get(id);
                adjacentNodes[i] = nCell;
            }
        }

        return adjacentNodes;
    }

    /**
     * Returns a valid NavCell for viable navigation and null for invalid
     * navigation. For the order of the returned array please refer to
     * NavCell.Direction
     *
     * @return array of adjacentNodes
     */
    public RCell[] getAdjacentRNodes() {
        for (int i = 0; i < VALID_DIR; i++) {
            Point temp = adjNodes[i];
            if (temp.x < 0 || temp.x >= frameWidth || temp.y < 0 || temp.y >= frameHeight) {
                adjacentRNodes[i] = null;
                continue;
            } else {
                int id = ReinforcementLearnerUtil.findPoint(temp, rGridMap).getId();
                adjacentRNodes[i] = new RCell(temp, id, cellWidth);

                if (intersectsWall(temp)) {
                    adjacentRNodes[i].setReward(rlMinReward);
                } else {
                    adjacentRNodes[i].setReward(rlMaxReward);
                }
            }
        }

        return adjacentRNodes;
    }

    public int getLidarCost() {
        return lidarCost;
    }

    public void setLidarCost(int lidarCost) {
        this.lidarCost = lidarCost;
    }

    private boolean intersectsWall(Point temp) {
        boolean intersects = false;
        for (Wall w : wallBuilder.getWalls()) {
            if (w.intersects(temp)) {
                intersects = true;
                break;
            }
        }

        lidarCost++;

        return intersects;
    }
}
