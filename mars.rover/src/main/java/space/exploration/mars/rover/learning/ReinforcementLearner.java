package space.exploration.mars.rover.learning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.environment.Wall;
import space.exploration.mars.rover.environment.WallBuilder;
import space.exploration.mars.rover.navigation.AdjacencyCalculator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by sanket on 5/23/17.
 */
public class ReinforcementLearner {
    public static final  double MIN_REWARD     = -1.0d;
    public static final  double NORMAL_REWARD  = 1.0D;
    public static final  double MAX_REWARD     = 10.0d;
    private static final int    MAX_ITERATIONS = 1000;

    private Logger logger = LoggerFactory.getLogger(ReinforcementLearner.class);

    private Properties marsConfig = null;
    private RCell[][]  navGrid    = null;

    private int lidarUsage = 0;
    private int frameWidth = 0;
    private int cellWidth  = 0;

    private WallBuilder wallBuilder = null;
    private RCell       source      = null;
    private RCell       destination = null;

    private double gamma = 0.0d;
    private double alpha = 0.0d;

    public ReinforcementLearner(Properties marsConfig) {
        this.marsConfig = marsConfig;

        this.frameWidth = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.FRAME_WIDTH_PROPERTY));
        this.cellWidth = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));
        this.wallBuilder = new WallBuilder(marsConfig);
        populateGrid();
        configureAdjacency();
    }

    public WallBuilder getWallBuilder() {
        return wallBuilder;
    }

    public void setWallBuilder(WallBuilder wallBuilder) {
        this.wallBuilder = wallBuilder;
    }

    public int getLidarUsage() {
        return lidarUsage;
    }

    public void train(Point source, Point dest) {
        this.destination = ReinforcementLearnerUtil.findPoint(dest, navGrid);
        this.source = ReinforcementLearnerUtil.findPoint(source, navGrid);

        destination.setReward(MAX_REWARD);
        destination.setqValue(MAX_REWARD);

        ReinforcementLearnerUtil.setDestinationReward(navGrid, destination, MAX_REWARD);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            RCell current = ReinforcementLearnerUtil.getRandomSource(navGrid);
            //episode
            while (!current.equals(destination)) {
                RCell  temp = current.getRandomAction();
                RCell  next = ReinforcementLearnerUtil.findPoint(temp.getCenter(), navGrid);
                double maxQ = next.getBestAction().getqValue();
                double newQValue = (1.0d - alpha) * current.getqValue() + alpha * (next.getReward() + gamma * maxQ -
                        current.getqValue());
                temp.setqValue(newQValue);

                current = next;
            }
            System.out.println(i);
        }
    }

    public void train(Point source, Point dest, List<Point> captureExploration) {
        this.destination = ReinforcementLearnerUtil.findPoint(dest, navGrid);
        this.source = ReinforcementLearnerUtil.findPoint(source, navGrid);

        destination.setReward(MAX_REWARD);
        destination.setqValue(MAX_REWARD);

        ReinforcementLearnerUtil.setDestinationReward(navGrid, destination, MAX_REWARD);

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            RCell current = ReinforcementLearnerUtil.getRandomSource(navGrid);
            //episode
            while (!current.equals(destination)) {
                RCell  temp = current.getRandomAction();
                RCell  next = ReinforcementLearnerUtil.findPoint(temp.getCenter(), navGrid);
                double maxQ = next.getBestAction().getqValue();
                double newQValue = (1.0d - alpha) * current.getqValue() + alpha * (next.getReward() + gamma * maxQ -
                        current.getqValue());
                temp.setqValue(newQValue);

                captureExploration.add(next.getCenter());
                current = next;
            }
            System.out.println(i);
        }
    }

    public List<Point> getShortestPath() {
        List<Point> shortestPath = new ArrayList<>();

        RCell temp = source;
        int   i    = 0;
        while (true) {
            if (!shortestPath.contains(temp)) {
                shortestPath.add(temp.getCenter());
            }

            if (temp.equals(destination)) {
                return shortestPath;
            }

            RCell bestAction = temp.getBestAction();
            temp = ReinforcementLearnerUtil.findPoint(bestAction.getCenter(), navGrid);
            i++;

            if (i == MAX_ITERATIONS * 10) {
                logger.error(" Could not find path between " + source.toString() + " and " + destination.toString());
                return shortestPath;
            }
        }
    }

    public void configureLearner(double discount, double stepSize) {
        this.gamma = discount;
        this.alpha = stepSize;
    }

    public List<Point> getPath() {
        return null;
    }

    public RCell[][] getNavGrid() {
        return navGrid;
    }

    private void populateGrid() {
        int numCells = frameWidth / cellWidth;
        navGrid = new RCell[numCells][numCells];

        int id = 0;
        for (int i = 0; i < numCells; i++) {
            for (int j = 0; j < numCells; j++) {
                Point point = new Point(i * cellWidth, j * cellWidth);
                navGrid[i][j] = new RCell(point, id++, cellWidth);

                if (intersects(navGrid[i][j])) {
                    navGrid[i][j].setReward(MIN_REWARD);
                } else {
                    navGrid[i][j].setReward(NORMAL_REWARD);
                }
            }
        }
    }

    private void configureAdjacency() {
        for (int i = 0; i < navGrid[0].length; i++) {
            for (int j = 0; j < navGrid[0].length; j++) {
                RCell               current             = navGrid[i][j];
                AdjacencyCalculator adjacencyCalculator = new AdjacencyCalculator(current.getCenter(), marsConfig);
                adjacencyCalculator.setrGridMap(navGrid);
                current.setAdjacentNodes(adjacencyCalculator.getAdjacentRNodes());
            }
        }
    }

    private boolean intersects(RCell rCell) {
        boolean intersects = false;

        for (Wall w : wallBuilder.getWalls()) {
            lidarUsage++;
            if (w.intersects(rCell.getCenter())) {
                intersects = true;
                break;
            }
        }

        return intersects;
    }

    private RCell getRandomNextNode(final RCell current) {
        RCell next = null;
        while (true) {
            int randomChoice = ThreadLocalRandom.current().nextInt(0, 4);
            switch (randomChoice) {
                case 0: {
                    next = ReinforcementLearnerUtil.getLeft(current, navGrid, cellWidth);
                }
                break;
                case 1: {
                    next = ReinforcementLearnerUtil.getRight(current, navGrid, cellWidth, frameWidth);
                }
                break;
                case 2: {
                    next = ReinforcementLearnerUtil.getTop(current, navGrid, cellWidth);
                }
                break;
                case 3: {
                    next = ReinforcementLearnerUtil.getBottom(current, navGrid, cellWidth, frameWidth);
                }
            }

            if (next != null) {
                return next;
            }
        }
    }
}
