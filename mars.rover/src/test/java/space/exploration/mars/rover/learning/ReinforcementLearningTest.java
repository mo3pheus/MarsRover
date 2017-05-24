package space.exploration.mars.rover.learning;

import space.exploration.mars.rover.bootstrap.MatrixCreation;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.utils.TrackingAnimationUtil;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by sanketkorgaonkar on 5/23/17.
 */
public class ReinforcementLearningTest {

    public static void main(String[] args) {
        Properties marsConfig = null;
        try {
            marsConfig = MatrixCreation.getMatrixConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Point>          captureExploration = new ArrayList<>();
        ReinforcementLearner learner            = new ReinforcementLearner(marsConfig);
        learner.configureLearner(0.6d, 0.99d);
        learner.train(new Point(0, 250), new Point(650, 375), captureExploration);

        int i = 0;
        for (Point temp : learner.getShortestPath()) {
            System.out.println(" i = " + i++ + " " + temp.toString());
        }

        try {
//            MarsArchitect marsArchitect = new MarsArchitect(MatrixCreation.getMatrixConfig(), captureExploration);
//            marsArchitect.getMarsSurface().dispose();
            System.out.println("Number of points explored = " + captureExploration.size());
            System.out.println("Lidar cost = " + learner.getLidarUsage());
            MarsArchitect marsArchitect1 = new MarsArchitect(MatrixCreation.getMatrixConfig(), TrackingAnimationUtil
                    .getAnimationCalibratedRobotPath(learner.getShortestPath(),
                            Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.ANIMATION_STEP_SIZE))
                    ));
            Thread.sleep(5000);
            marsArchitect1.getMarsSurface().dispose();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
