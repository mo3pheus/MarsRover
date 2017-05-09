package space.exploration.mars.rover.animation;

import space.exploration.mars.rover.environment.Cell;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

/**
 * Created by sanketkorgaonkar on 5/9/17.
 */
public class CameraAnimationEngine {
    private static final int CAMERA_DEPTH = 99;
    private final int        NUM_FLASHES  = 10;
    private       Point      location     = null;
    private       long       shutterSpeed = 0l;
    private       int        cellWidth    = 0;
    private Cell robot = null;
    private JFrame marsSurface = null;
    private       Properties marsConfig   = null;

    public CameraAnimationEngine(Properties marsConfig, Point location, long shutterSpeed, int cellWidth) {
        this.location = location;
        this.cellWidth = cellWidth;
        this.shutterSpeed = shutterSpeed;
        this.marsConfig = marsConfig;
    }

    public void setRobot(Cell robot){ this.robot = robot;}

    public void setMarsSurface(JFrame marsSurface){ this.marsSurface = marsSurface;}

    public void clickCamera() {
        Cell flash = new Cell(marsConfig);
        flash.setCellWidth(cellWidth);
        flash.setColor(Color.white);
        flash.setLocation(location);


        JLayeredPane contentPane = AnimationUtil.getContent(marsConfig);
        contentPane.add(robot, Cell.ROBOT_DEPTH);
        for(int i = 0; i < NUM_FLASHES; i++){
            contentPane.add(flash);
            marsSurface.setContentPane(contentPane);
            marsSurface.setVisible(true);
            try {
                Thread.sleep(this.shutterSpeed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            contentPane.remove(flash);
        }

    }
}
