package space.exploration.mars.rover.learning;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by sanketkorgaonkar on 5/23/17.
 */
public class ReinforcementLearnerUtil {

    public static RCell getRandomSource(RCell[][] grid) {
        int i = ThreadLocalRandom.current().nextInt(0, grid[0].length);
        int j = ThreadLocalRandom.current().nextInt(0, grid[0].length);

        return grid[i][j];
    }

    public static void setDestinationReward(RCell[][] grid, RCell destination, double reward) {
        for (int i = 0; i < grid[0].length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j].equals(destination)) {
                    grid[i][j].setqValue(reward);
                    grid[i][j].setReward(reward);
                }
                for (RCell adNode : grid[i][j].getAdjacentNodes()) {
                    if (adNode != null && adNode.getCenter().equals(destination.getCenter())) {
                        adNode.setqValue(reward);
                        adNode.setReward(reward);
                    }
                }
            }
        }
    }

    public static RCell findPoint(Point point, RCell[][] grid) {
        for (int i = 0; i < grid[0].length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j].getCenter().equals(point)) {
                    return grid[i][j];
                }
            }
        }
        return null;
    }

    public static RCell getLeft(RCell rCell, RCell[][] grid, int cellWidth) {
        Point center = rCell.getCenter();
        int   newX   = center.x - cellWidth;

        return (newX < 0) ? null : findPoint(new Point(newX, center.y), grid);
    }

    public static RCell getRight(RCell rCell, RCell[][] grid, int cellWidth, int frameWidth) {
        Point center = rCell.getCenter();
        int   newX   = center.x + cellWidth;

        return (newX > frameWidth) ? null : findPoint(new Point(newX, center.y), grid);
    }

    public static RCell getTop(RCell rCell, RCell[][] grid, int cellWidth) {
        Point center = rCell.getCenter();
        int   newY   = center.y - cellWidth;

        return (newY < 0) ? null : findPoint(new Point(center.x, newY), grid);
    }

    public static RCell getBottom(RCell rCell, RCell[][] grid, int cellWidth, int frameWidth) {
        Point center = rCell.getCenter();
        int   newY   = center.y + cellWidth;

        return (newY > frameWidth) ? null : findPoint(new Point(center.x, newY), grid);
    }

    public static RCell getBest(RCell rCell, RCell[][] grid, int cellWidth, int frameWidth) {
        RCell best = null;

        List<RCell> adjacentNodes = new ArrayList<>();
        RCell       left          = ReinforcementLearnerUtil.getLeft(rCell, grid, cellWidth);
        if (left != null) {
            adjacentNodes.add(left);
        }

        RCell right = ReinforcementLearnerUtil.getRight(rCell, grid, cellWidth, frameWidth);
        if (right != null) {
            adjacentNodes.add(right);
        }

        RCell top = ReinforcementLearnerUtil.getTop(rCell, grid, cellWidth);
        if (top != null) {
            adjacentNodes.add(top);
        }

        RCell bottom = ReinforcementLearnerUtil.getBottom(rCell, grid, cellWidth, frameWidth);
        if (bottom != null) {
            adjacentNodes.add(bottom);
        }

        double maxGVal = -1000.0d;
        for (RCell temp : adjacentNodes) {
            if (temp.getqValue() > maxGVal) {
                maxGVal = temp.getqValue();
                best = temp;
            }
        }

        return best;
    }
}
