package space.exploration.mars.rover.environment;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static space.exploration.mars.rover.environment.Cell.CELL_WIDTH;

public class DanScanCell extends VirtualElement {
    private static final String        DAN_ICON_FILE = "/h2o-icon-25-25.jpg";
    private              BufferedImage danImg        = null;
    private              Properties    marsConfig    = null;
    private              Point         location      = null;
    private              int           cellWidth     = 0;

    public DanScanCell(Properties marsConfig, Point location) {
        this.marsConfig = marsConfig;
        this.location = location;
        try {
            URL url = WeatherScanCell.class.getResource(DAN_ICON_FILE);
            danImg = ImageIO.read(url);
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
        g2.drawImage(danImg, null, location.x+2, location.y+2);
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
