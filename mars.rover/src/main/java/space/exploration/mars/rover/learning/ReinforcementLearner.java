package space.exploration.mars.rover.learning;

import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.environment.WallBuilder;

import java.awt.*;
import java.util.Properties;

/**
 * Created by sanket on 5/23/17.
 */
public class ReinforcementLearner {
    private static final double      ETA         = 0.99d;
    private              WallBuilder wallBuilder = null;

    private RCell[][]  navGrid    = null;
    private Properties marsConfig = null;
    private int        frameWidth = 0;
    private int        cellWidth  = 0;

    private Point sourceCell      = null;
    private Point destinationCell = null;

    public ReinforcementLearner(Properties marsConfig) {
        this.marsConfig = marsConfig;
        this.frameWidth = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.FRAME_WIDTH_PROPERTY));
        this.cellWidth = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.CELL_WIDTH_PROPERTY));
        initializeGrid();
    }

    public void setSourceCell(Point sourceCell) {
        this.sourceCell = sourceCell;
    }

    public void setDestinationCell(Point destinationCell) {
        this.destinationCell = destinationCell;
    }

    private RCell getNextCell(RCell current){
        RCell next = new RCell();

        

        return next;
    }

    private void initializeGrid() {
        int numCells = frameWidth / cellWidth;
        this.navGrid = new RCell[numCells][numCells];
        int num = 0;
        for (int i = 0; i < numCells; i++) {
            for (int j = 0; j < numCells; j++) {
                navGrid[i][j] = new RCell();
                navGrid[i][j].setI(num++);
                navGrid[i][j].setP(new Point(i * cellWidth, j * cellWidth));
                navGrid[i][j].setQ(0.0d);
            }
        }
    }
}
