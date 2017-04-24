package space.exploration.mars.rover.navigation;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import space.exploration.mars.rover.environment.EnvironmentUtils;

public class NavUtil {
	public static Map<Integer, NavCell> populateGridMap(Properties matrixConfig) {
		int totalHeight = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.FRAME_HEIGHT_PROPERTY));
		int cellWidth = Integer.parseInt(matrixConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));

		int pointCount = totalHeight / cellWidth;
		int id = 0;
		Map<Integer, NavCell> gridMap = new HashMap<Integer, NavCell>();
		for (int i = 0; i < pointCount; i++) {
			for (int j = 0; j < pointCount; j++) {
				Point tempPoint = new Point(j * cellWidth, i * cellWidth);
				NavCell nCell = new NavCell(tempPoint, id);
				gridMap.put(id, nCell);
				id++;
			}
		}
		return gridMap;
	}

	public static int findNavId(Map<Integer, NavCell> gridMap, Point center) {
		int id = -1;

		for (int i : gridMap.keySet()) {
			NavCell temp = gridMap.get(i);
			if (temp.getCenter().equals(center)) {
				id = i;
				break;
			}
		}

		return id;
	}

	public static void addUniqueToOpen(List<NavCell> source, List<NavCell> open, List<NavCell> closed) {
		for (NavCell nCell : source) {
			if (!open.contains(nCell) && !closed.contains(nCell)) {
				open.add(nCell);
			}
		}
	}

	public static final double getGCost(NavCell current, NavCell start, int cellWidth) {
		if (current == null || current.equals(start)) {
			return 0.0d;
		} else
			return cellWidth + getGCost(current.getParent(), start, cellWidth);
	}

	public static final List<NavCell> getAdjNodesFromGrid(Map<Integer, NavCell> gridMap, NavCell[] adjNodes) {
		List<NavCell> adjacentNodes = new ArrayList<NavCell>();

		for (NavCell nCell : adjNodes) {
			int id = NavUtil.findNavId(gridMap, nCell.getCenter());
			adjacentNodes.add(gridMap.get(id));
		}
		return adjacentNodes;
	}

	/**
	 * Returns the index of the cell with the lowest fScore.
	 * 
	 * @param listCells
	 * @param start
	 * @param end
	 * @param cellWidth
	 * @return
	 */
	public static final int getMinFCell(List<NavCell> listCells, NavCell start, NavCell end, int cellWidth) {
		double minFScore = Double.MAX_VALUE;
		int minFId = 0;

		for (int i = 0; i < listCells.size(); i++) {
			NavCell nCell = listCells.get(i);
			double currentFScore = nCell.getfCost();
			if (currentFScore < minFScore) {
				minFId = i;
				minFScore = currentFScore;
			}
		}

		return minFId;
	}
}
