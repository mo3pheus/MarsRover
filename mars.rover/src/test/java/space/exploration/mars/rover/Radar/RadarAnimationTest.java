package space.exploration.mars.rover.Radar;

import space.exploration.mars.rover.animation.RadarAnimationEngine;
import space.exploration.mars.rover.bootstrap.MatrixCreation;

import java.awt.*;

/**
 * Created by sanket on 6/3/17.
 */
public class RadarAnimationTest {

    public static void main(String[] args) throws Exception {
        RadarAnimationEngine radarAnimationEngine = new RadarAnimationEngine(MatrixCreation.getMatrixConfig());
        radarAnimationEngine.setUpRadarWindow();
        radarAnimationEngine.renderLaserAnimation();
        radarAnimationEngine.renderPingAnimation(new Point(200, 200),radarAnimationEngine.getRadarWindow());
    }
}
