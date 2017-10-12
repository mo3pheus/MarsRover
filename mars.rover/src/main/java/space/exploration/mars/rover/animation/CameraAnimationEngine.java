package space.exploration.mars.rover.animation;

import space.exploration.mars.rover.environment.Cell;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

/**
 * Created by sanketkorgaonkar on 5/9/17.
 */
public class CameraAnimationEngine {
    private static final Integer    CAMERA_DEPTH = new Integer(99);
    protected            Point      location     = null;
    protected            long       shutterSpeed = 0l;
    protected            Cell       robot        = null;
    protected            JFrame     marsSurface  = null;
    private              Properties marsConfig   = null;

    public CameraAnimationEngine(Properties marsConfig, Point location, long shutterSpeed, int cellWidth) {
        this.location = location;
        int cellWidth1 = cellWidth;
        this.shutterSpeed = shutterSpeed;
        this.marsConfig = marsConfig;
    }

    public void setRobot(Cell robot) {
        this.robot = robot;
        System.out.println("Robot color is = " + robot.getColor().toString());
    }

    public void setMarsSurface(JFrame marsSurface) {
        this.marsSurface = marsSurface;
    }

    public void clickCamera() {
        JLayeredPane contentPane = AnimationUtil.getContent(marsConfig);
        contentPane.add(robot, Cell.ROBOT_DEPTH);
        int NUM_FLASHES = 10;
        for (int i = 0; i < NUM_FLASHES; i++) {
            Cell flash = new Cell(marsConfig);
            flash.setCellWidth(robot.getCellWidth());
            flash.setColor(Color.white);
            flash.setLocation(location);
            contentPane.add(flash, new Integer(Cell.ROBOT_DEPTH + 1));
            marsSurface.setContentPane(contentPane);
            marsSurface.setVisible(true);
            try {
                Thread.sleep(this.shutterSpeed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            contentPane.remove(flash);

            flash.setColor(Color.black);
            contentPane.add(flash, new Integer(Cell.ROBOT_DEPTH + 1));
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
