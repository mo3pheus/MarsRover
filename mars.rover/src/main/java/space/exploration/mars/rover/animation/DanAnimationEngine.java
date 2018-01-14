package space.exploration.mars.rover.animation;

import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.environment.DanScanCell;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class DanAnimationEngine {
    Properties marsConfig  = null;
    Point      location    = null;
    long       flashSpeed  = 0l;
    JFrame     marsSurface = null;

    public DanAnimationEngine(Properties marsConfig, Point location, long flashSpeed, JFrame marsSurface) {
        this.marsConfig = marsConfig;
        this.location = location;
        this.marsSurface = marsSurface;
        this.flashSpeed = flashSpeed;
    }

    public void updateLocation(Point location) {
        this.location = location;
    }

    public void renderDanAnimation() {
        JLayeredPane contentPane = AnimationUtil.getContent(marsConfig);
        int NUM_FLASHES = 10;
        for (int i = 0; i < NUM_FLASHES; i++) {
            Cell blueCell = new Cell(marsConfig);
            blueCell.setCellWidth(15);
            blueCell.setLocation(location);
            //blueCell.setColor(new Color(102,0,102));
            blueCell.setColor(new Color(51,153,255));
            contentPane.add(blueCell, new Integer(Cell.ROBOT_DEPTH + 1));
            marsSurface.setContentPane(contentPane);
            marsSurface.setVisible(true);
            try {
                Thread.sleep(this.flashSpeed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            contentPane.remove(blueCell);

            blueCell = new Cell(marsConfig);
            blueCell.setCellWidth(15);
            blueCell.setLocation(location);
            blueCell.setColor(new Color(51,153,255));
            blueCell.setVisible(false);
            contentPane.add(blueCell, new Integer(Cell.ROBOT_DEPTH + 1));
            marsSurface.setContentPane(contentPane);
            marsSurface.setVisible(true);
            try {
                Thread.sleep(this.flashSpeed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            contentPane.remove(blueCell);
        }
    }
}
