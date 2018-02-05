package space.exploration.mars.rover.learning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.environment.Wall;
import space.exploration.mars.rover.environment.WallBuilder;
import space.exploration.mars.rover.navigation.AdjacencyCalculator;
import space.exploration.mars.rover.utils.HeatMap;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by sanket on 5/23/17.
 */
public class ReinforcementLearner {
    public static final String RL_ENGINE_PREFIX = "mars.rover.rlEngine";

    private Logger logger = LoggerFactory.getLogger(ReinforcementLearner.class);

    private          Properties marsConfig = null;
    private volatile RCell[][]  navGrid    = null;

    private int lidarUsage       = 0;
    private int frameWidth       = 0;
    private int cellWidth        = 0;
    private int explorationSteps = 0;

    private WallBuilder wallBuilder = null;
    private RCell       source      = null;
    private RCell       destination = null;

    private double  gamma         = 0.0d;
    private double  alpha         = 0.0d;
    private int     minReward     = 0;
    private int     maxReward     = 0;
    private int     normalReward  = 0;
    private int     maxIterations = 0;
    private HeatMap heatMap       = new HeatMap();

    public ReinforcementLearner(Properties marsConfig) {
        this.marsConfig = marsConfig;

        this.frameWidth = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.FRAME_WIDTH_PROPERTY));
        this.cellWidth = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));
        this.wallBuilder = new WallBuilder(marsConfig);

        this.alpha = Double.parseDouble(marsConfig.getProperty(RL_ENGINE_PREFIX + ".discountRate"));
        this.gamma = Double.parseDouble(marsConfig.getProperty(RL_ENGINE_PREFIX + ".stepSize"));
        this.minReward = Integer.parseInt(marsConfig.getProperty(RL_ENGINE_PREFIX + ".minReward"));
        this.maxIterations = Integer.parseInt(marsConfig.getProperty(RL_ENGINE_PREFIX + ".maxIterations"));
        this.maxReward = Integer.parseInt(marsConfig.getProperty(RL_ENGINE_PREFIX + ".maxReward"));
        this.normalReward = Integer.parseInt(marsConfig.getProperty(RL_ENGINE_PREFIX + ".normalReward"));

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
        logger.debug(" rlEngine training triggered at SCET = " + System.currentTimeMillis() + " source = " + source
                .toString() + " destination = " + dest.toString());
        this.destination = ReinforcementLearnerUtil.findPoint(dest, navGrid);
        this.source = ReinforcementLearnerUtil.findPoint(source, navGrid);

        destination.setReward(maxReward);
        destination.setqValue(minReward);

        ReinforcementLearnerUtil.setDestinationReward(navGrid, destination, maxReward);

        for (int i = 0; i < maxIterations; i++) {
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
                explorationSteps++;
            }
            //System.out.println("Reinforcement Learning progress = " + (i + 1) + "%");
        }
        heatMap.updateReinforcementLearner(this);
        heatMap.renderHeatMap();
        logger.info(" rlEngine logging totalExplorationSteps = " + explorationSteps);
    }

    @Deprecated
    public void train(Point source, Point dest, List<Point> captureExploration) {
        logger.debug(" rlEngine training triggered at SCET = " + System.currentTimeMillis() + " source = " + source
                .toString() + " destination = " + dest.toString());
        this.destination = ReinforcementLearnerUtil.findPoint(dest, navGrid);
        this.source = ReinforcementLearnerUtil.findPoint(source, navGrid);

        destination.setReward(maxReward);
        destination.setqValue(maxReward);

        ReinforcementLearnerUtil.setDestinationReward(navGrid, destination, maxReward);

        for (int i = 0; i < maxIterations; i++) {
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
                explorationSteps++;
                current = next;
            }
            System.out.println("Reinforcement Learning progress = " + (i + 1) + "%");
        }
        logger.debug(" rlEngine logging totalExplorationSteps = " + explorationSteps);
    }

    public List<Point> getShortestPath() {
        List<Point> shortestPath = new ArrayList<>();

        RCell temp = source;
        int   i    = 0;
        while (true) {
            if (!shortestPath.contains(temp.getCenter())) {
                shortestPath.add(temp.getCenter());
            }

            if (temp.equals(destination)) {
                return shortestPath;
            }

            RCell bestAction = temp.getBestAction();
            temp = ReinforcementLearnerUtil.findPoint(bestAction.getCenter(), navGrid);
            i++;

            if (i == maxIterations * 10) {
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

    public double getGamma() {
        return gamma;
    }

    public double getAlpha() {
        return alpha;
    }

    public int getMinReward() {
        return minReward;
    }

    public int getMaxReward() {
        return maxReward;
    }

    public int getNormalReward() {
        return normalReward;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    private void populateGrid() {
        int numCells = frameWidth / cellWidth;
        navGrid = new RCell[numCells][numCells];

        int id = 0;
        for (int i = 0; i < numCells; i++) {
            for (int j = 0; j < numCells; j++) {
                Point point = new Point(i * cellWidth, j * cellWidth);
                navGrid[i][j] = new RCell(point, id++);

                if (intersects(navGrid[i][j])) {
                    navGrid[i][j].setReward(minReward);
                } else {
                    navGrid[i][j].setReward(normalReward);
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
