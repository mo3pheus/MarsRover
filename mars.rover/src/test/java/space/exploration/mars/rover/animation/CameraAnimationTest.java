package space.exploration.mars.rover.animation;

import space.exploration.mars.rover.bootstrap.MatrixCreation;
import space.exploration.mars.rover.environment.MarsArchitect;

import java.awt.*;

/**
 * Created by sanketkorgaonkar on 6/8/17.
 */
public class CameraAnimationTest {
    public static void main(String[] args) {
        System.out.println(10 / 3);
        try {
            MarsArchitect architect = new MarsArchitect(MatrixCreation.getConfig());
            CameraAnimationEngine cameraAnimationEngine = new CameraAnimationEngine(MatrixCreation.getConfig(),
                                                                                    new Point(50, 50), 150l);
            cameraAnimationEngine.setMarsSurface(architect.getMarsSurface());
            cameraAnimationEngine.setRobot(architect.getRobot());
            cameraAnimationEngine.clickCamera();
            architect.getMarsSurface().dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
