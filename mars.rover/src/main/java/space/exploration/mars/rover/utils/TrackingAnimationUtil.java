package space.exploration.mars.rover.utils;

import space.exploration.mars.rover.animation.AnimationUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sanketkorgaonkar on 5/24/17.
 */
public class TrackingAnimationUtil {

    public static List<Point> getAnimationCalibratedRobotPath(List<Point> robotPath, int animationStepSize) {
        java.util.List<Point> calibratedPath = new ArrayList<Point>();
        if (robotPath != null) {
            for (int i = 0; i < robotPath.size() - 1; i++) {
                Point                 start      = robotPath.get(i);
                Point                 end        = robotPath.get(i + 1);
                java.util.List<Point> tempPoints = AnimationUtil.generateRobotPositions(start, end, animationStepSize);
                calibratedPath.addAll(tempPoints);
            }
        }
        return calibratedPath;
    }
}
