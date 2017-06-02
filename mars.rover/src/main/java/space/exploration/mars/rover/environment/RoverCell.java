package space.exploration.mars.rover.environment;

import java.awt.*;
import java.util.Properties;

/**
 * Created by sanket on 5/31/17.
 */
public class RoverCell extends Cell {
    public static final  int        ROVER_CELL_DEPTH   = 98;
    private static final double     DEGREE_OF_ROTATION = 45.0D;
    private              Properties marsConfig         = null;

    public RoverCell(Properties marsConfig) {
        super(marsConfig);
        this.marsConfig = marsConfig;
    }

    @Override
    public void draw(Graphics2D g2) {
        //g2.rotate(Math.toRadians(DEGREE_OF_ROTATION));
        g2.setColor(EnvironmentUtils.findColor(marsConfig.getProperty(EnvironmentUtils.OLD_ROVERS_COLOR)));
        g2.fill(this.getCell());
    }
}
