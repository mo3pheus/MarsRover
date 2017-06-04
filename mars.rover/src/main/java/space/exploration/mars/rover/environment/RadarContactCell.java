package space.exploration.mars.rover.environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Properties;

/**
 * Created by sanket on 6/1/17.
 */
public class RadarContactCell extends VirtualElement {
    public static        Color      BLIP_COLOR       = Color.green;
    private static final int        CONTACT_DIAMETER = 7;
    private static       Logger     logger           = LoggerFactory.getLogger(RadarContactCell.class);
    private              Color      color            = null;
    private              Properties contactConfig    = null;
    private              Point      location         = null;

    public RadarContactCell(Properties config, Point location, Color color) {
        this.color = color;
        this.contactConfig = config;
        this.location = location;
        build();
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(color);
        g2.fillOval(location.x, location.y, CONTACT_DIAMETER, CONTACT_DIAMETER);
    }

    @Override
    public Color getColor() {
        return EnvironmentUtils.findColor(contactConfig.getProperty(EnvironmentUtils.RADAR_CONTACT_COLOR));
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public void build() {
        super.setMatrixConfig(contactConfig);
        super.setLayout();
    }

    @Override
    public void paint(Graphics g) {
        draw((Graphics2D) g);
    }

    @Override
    public Point getLocation() {
        return location;
    }

    @Override
    public void setLocation(Point location) {
        this.location = location;
    }

    public Rectangle2D getContactRect(){
        return new Rectangle2D.Double(getLocation().x, getLocation().y, 1, 1);
    }
}
