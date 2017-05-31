package space.exploration.mars.rover.learning;

import space.exploration.mars.rover.bootstrap.MatrixCreation;
import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.utils.TrackingAnimationUtil;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by sanketkorgaonkar on 5/23/17.
 */
public class ReinforcementLearningTest {

    public static void main(String[] args) {
        long stTime = System.currentTimeMillis();
        System.out.println("Start time = " + stTime);
        Properties marsConfig = null;
        try {
            marsConfig = MatrixCreation.getMatrixConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Point>          captureExploration = new ArrayList<>();
        ReinforcementLearner learner            = new ReinforcementLearner(marsConfig);
        learner.configureLearner(0.6d, 0.99d);
        learner.train(new Point(0, 250), new Point(0, 375), captureExploration);

        int k = 0;
        for (Point temp : learner.getShortestPath()) {
            System.out.println(" k = " + k++ + " " + temp.toString());
        }

        try {
            MarsArchitect marsArchitect = new MarsArchitect(MatrixCreation.getMatrixConfig(), captureExploration);
            marsArchitect.getMarsSurface().dispose();
            System.out.println("Number of points explored = " + captureExploration.size());
            System.out.println("Lidar cost = " + learner.getLidarUsage());
            MarsArchitect marsArchitect1 = new MarsArchitect(MatrixCreation.getMatrixConfig(), TrackingAnimationUtil
                    .getAnimationCalibratedRobotPath(learner.getShortestPath(),
                            Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.ANIMATION_STEP_SIZE))
                    ));
            Thread.sleep(5000);
            //marsArchitect1.getMarsSurface().dispose();

            Map<Point, Double> heatMap = new HashMap<>();
            RCell[][]          navGrid = learner.getNavGrid();
            for (int i = 0; i < navGrid[0].length; i++) {
                for (int j = 0; j < navGrid[0].length; j++) {
                    RCell best = navGrid[i][j].getBestAction();
                    if (heatMap.get(best.getCenter()) != null) {
                        Double score = heatMap.get(best.getCenter());
                        heatMap.put(best.getCenter(), score + best.getqValue());
                    } else {
                        heatMap.put(best.getCenter(), best.getqValue());
                    }
                }
            }

            double   maxScore = 0.0d;
            Point    maxPoint = null;
            double[] scores   = new double[heatMap.keySet().size()];
            int      i        = 0;
            for (Point current : heatMap.keySet()) {
                scores[i] = heatMap.get(current).doubleValue();
                if (heatMap.get(current) > maxScore) {
                    maxScore = heatMap.get(current).doubleValue();
                    maxPoint = current;
                }
            }
            System.out.println(" Max Score is = " + maxScore + " for point = " + maxPoint.toString());

            marsArchitect1.getMarsSurface().repaint();
            Container         contentPane  = marsArchitect1.getMarsSurface().getContentPane();
            Map<Point, Color> heatColorMap = new HashMap<>();
            for (Point current : heatMap.keySet()) {
                heatColorMap.put(current, getHeatMapColor(heatMap.get(current).intValue(), maxScore));
                Cell cell = new Cell(marsConfig);
                cell.setLocation(current);
                cell.setColor(heatColorMap.get(current));
                cell.setCellWidth(marsArchitect1.getCellWidth());
                contentPane.add(cell, 200);
                System.out.println(" Current = " + current.toString() + " Count = " + heatMap.get(current) + " Color " +
                                   "returned = " + heatColorMap.get(current).toString());
            }
            marsArchitect1.getMarsSurface().setContentPane(contentPane);
            marsArchitect1.getMarsSurface().repaint();

            System.out.println(" Red looks like follows:: " + Color.red.toString());
            System.out.println("Time elapsed = " + (System.currentTimeMillis() - stTime));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static Color getHeatMapColor(double score, double maxScore) {
        if (score < 0.0d) {
            return Color.black;
        } else if (score > 0.0d && score < 2.5d) {
            return Color.red;
        } else if (score > 2.5d && score < 10.0d) {
            return Color.orange;
        } else if (score > 10.0d && score < 20.0d) {
            return Color.yellow;
        } else {
            return Color.white;
        }
    }
}
