package space.exploration.mars.rover.navigation;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NavCell implements Comparator<NavCell> {
    private NavCell[] adjacentNodes = new NavCell[Direction.values().length];
    private NavCell   parent        = null;
    private Point     center        = null;
    private int       id            = -1;
    private double    gCost         = 0.0d;
    private double    hCost         = 0.0d;
    private double    fCost         = 0.0d;

    public NavCell(Point center, int id) {
        this.center = center;
        this.id = id;
    }

    /**
     * @return the parent
     */
    public NavCell getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(NavCell parent) {
        this.parent = parent;
    }

    /**
     * @return the gCost
     */
    public double getgCost() {
        return gCost;
    }

    /**
     * @param gCost the gCost to set
     */
    public void setgCost(double gCost) {
        this.gCost = gCost;
    }

    /**
     * @return the fCost
     */
    public double getfCost() {
        return fCost;
    }

    public void setfCost(double fCost) {
        this.fCost = fCost;
    }

    public void sethCost(double hCost) {
        this.hCost = hCost;
    }

    /**
     * @return the hCost
     */
    public double gethCost() {
        return hCost;
    }

    /**
     * @param destination for which hCost is to be calculated.
     */
    public void sethCost(Point destination) {
        this.hCost = center.distance(destination);
    }

    public NavCell[] getAdjacentNodes() {
        List<NavCell> validNodes = new ArrayList<NavCell>();
        for (NavCell nCell : adjacentNodes) {
            if (nCell != null && nCell.getCenter() != null) {
                validNodes.add(nCell);
            }
        }
        NavCell[] vNodes = new NavCell[validNodes.size()];

        for (int i = 0; i < validNodes.size(); i++) {
            vNodes[i] = validNodes.get(i);
        }

        return vNodes;
    }

    public void setAdjacentNodes(NavCell[] adjacentNodes) {
        this.adjacentNodes = adjacentNodes;
    }

    public Point getCenter() {
        return center;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String toString() {
        return "";
    }

    public int compare(NavCell o1, NavCell o2) {
        if (o1.getCenter().equals(o2.getCenter())) {
            return 0;
        } else {
            return 1;
        }
    }

    public enum Direction {
        NORTH(0), SOUTH(1), WEST(2), EAST(3);
        private final int value;

        Direction(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
