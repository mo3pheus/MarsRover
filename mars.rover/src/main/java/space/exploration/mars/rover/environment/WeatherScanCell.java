package space.exploration.mars.rover.environment;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static space.exploration.mars.rover.environment.Cell.CELL_WIDTH;

public class WeatherScanCell extends VirtualElement {
    private BufferedImage weatherImg = null;
    private Properties    marsConfig = null;
    private Point         location   = null;
    private int           cellWidth  = 0;

    public WeatherScanCell(Properties marsConfig, Point location) {
        this.marsConfig = marsConfig;
        this.location = location;
        try {
            weatherImg = ImageIO.read(new File(marsConfig.getProperty("mars.rover.weather.station.image")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        build();
    }

    @Override
    public void draw(Graphics2D g2) {
        AffineTransform at        = new AffineTransform();
        int             cellWidth = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));
        at.scale(cellWidth, cellWidth);
        g2.drawImage(weatherImg, null, location.x, location.y);
    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public void build() {
        super.setMatrixConfig(marsConfig);
        super.setLayout();
        cellWidth = Integer.parseInt(marsConfig.getProperty(CELL_WIDTH));
    }

    @Override
    public void paint(Graphics g) {
        draw((Graphics2D) g);
    }
}
