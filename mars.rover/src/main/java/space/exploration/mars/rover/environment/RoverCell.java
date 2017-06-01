package space.exploration.mars.rover.environment;

import java.awt.*;
import java.util.Properties;

/**
 * Created by sanket on 5/31/17.
 */
public class RoverCell extends Cell {
    public static final  int    ROVER_CELL_DEPTH   = 98;
    private static final double DEGREE_OF_ROTATION = 90.0D;

    public RoverCell(Properties matrixConfig) {
        super(matrixConfig);
    }

    @Override
    public void draw(Graphics2D g2) {
        //g2.rotate(DEGREE_OF_ROTATION);
        g2.setColor(Color.blue);
        g2.fill(this.getCell());
    }
}
