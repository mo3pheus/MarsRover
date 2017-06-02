package space.exploration.mars.rover.animation;

import space.exploration.mars.rover.environment.EnvironmentUtils;

import javax.swing.*;
import java.util.Properties;

/**
 * Created by sanket on 5/30/17.
 */
public class RadarAnimationEngine {
    private final double     RADAR_SCALE_FACTOR = 0.2d;
    private       JFrame     radarWindow        = null;
    private       int        numRings           = 0;
    private       double     radius             = 0.0d;
    private       Properties radarConfig        = null;

    public RadarAnimationEngine(Properties radarConfig) {
        this.radarConfig = radarConfig;
        int frameWidth = Integer.parseInt(radarConfig.getProperty(EnvironmentUtils.FRAME_WIDTH_PROPERTY));
        radius = RADAR_SCALE_FACTOR * Math.sqrt(frameWidth * frameWidth);
    }
}
