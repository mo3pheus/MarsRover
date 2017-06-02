package space.exploration.mars.rover.environment;

import space.exploration.mars.rover.sensor.Radar;

import java.awt.*;
import java.util.Properties;

/**
 * Created by sanket on 6/1/17.
 */
public class RadarScanArea extends VirtualElement {
    private Properties        config            = null;
    private double            frameWidth        = 0.0d;
    private double            scaleFactor       = 0.0d;
    private double            radius            = 0.0d;
    private int               cellWidth         = 0;
    private RadarUnitCircle[] concentricCircles = null;

    public RadarScanArea(Properties config) {
        this.config = config;
        int frameWidth = Integer.parseInt(config.getProperty(EnvironmentUtils.FRAME_WIDTH_PROPERTY));
        scaleFactor = Double.parseDouble(config.getProperty(Radar.RADAR_PREFIX + ".scaleFactor"));
        this.frameWidth = scaleFactor * frameWidth;
        radius = scaleFactor * Math.sqrt(frameWidth * frameWidth);
        cellWidth = Integer.parseInt(config.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));
        int numRings = (int) (radius / cellWidth);
        concentricCircles = new RadarUnitCircle[numRings];
    }

    @Override
    public void draw(Graphics2D g2) {
        for (int i = 0; i < concentricCircles.length; i++) {
            concentricCircles[i].draw(g2);
        }
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public void build() {
        this.setMatrixConfig(config);
        this.setLayout((int) frameWidth);
        //go from largest to smallest
        for (int i = concentricCircles.length - 1; i >= 0; i--) {
            concentricCircles[i] = new RadarUnitCircle(cellWidth * i, config);
        }
    }

    @Override
    public void paint(Graphics g) {
        draw((Graphics2D) g);
    }

}
