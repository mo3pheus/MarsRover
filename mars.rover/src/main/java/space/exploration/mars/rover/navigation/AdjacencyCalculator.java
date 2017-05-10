package space.exploration.mars.rover.navigation;

import java.awt.Point;
import java.util.Map;
import java.util.Properties;

import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.environment.Wall;
import space.exploration.mars.rover.environment.WallBuilder;
import space.exploration.mars.rover.navigation.NavCell.Direction;

public class AdjacencyCalculator {
	private static final int		VALID_DIR		= 4;
	private int						cellWidth		= -1;
	private int						frameHeight		= -1;
	private int						frameWidth		= -1;
	private Point[]					adjNodes		= new Point[VALID_DIR];
	private NavCell[]				adjacentNodes	= new NavCell[VALID_DIR];
	private Map<Integer, NavCell>	gridMap			= null;
	private WallBuilder				wallBuilder		= null;

	public AdjacencyCalculator(Point center, Properties matrixConfig) {
		this.gridMap = NavUtil.populateGridMap(matrixConfig);
		this.wallBuilder = new WallBuilder(matrixConfig);

		cellWidth = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));
		frameHeight = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.FRAME_HEIGHT_PROPERTY));
		frameWidth = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.FRAME_WIDTH_PROPERTY));
		adjNodes[Direction.NORTH.getValue()] = new Point(center.x, center.y - cellWidth);
		adjNodes[Direction.SOUTH.getValue()] = new Point(center.x, center.y + cellWidth);
		adjNodes[Direction.EAST.getValue()] = new Point(center.x + cellWidth, center.y);
		adjNodes[Direction.WEST.getValue()] = new Point(center.x - cellWidth, center.y);
	}

	/**
	 * Returns a valid NavCell for viable navigation and null for invalid
	 * navigation. For the order of the returned array please refer to
	 * NavCell.Direction
	 * 
	 * @return
	 */
	public NavCell[] getAdjacentNodes() {
		for (int i = 0; i < VALID_DIR; i++) {
			Point temp = adjNodes[i];
			if (temp.x < 0 || temp.x >= frameWidth || temp.y < 0 || temp.y >= frameHeight || intersectsWall(temp)) {
				adjacentNodes[i] = null;
				continue;
			} else {
				int id = NavUtil.findNavId(gridMap, temp);
				NavCell nCell = gridMap.get(id);
				adjacentNodes[i] = nCell;
			}
		}

		return adjacentNodes;
	}

	private boolean intersectsWall(Point temp) {
		boolean intersects = false;
		for (Wall w : wallBuilder.getWalls()) {
			if (w.intersects(temp)) {
				intersects = true;
				break;
			}
		}

		return intersects;
	}
}
