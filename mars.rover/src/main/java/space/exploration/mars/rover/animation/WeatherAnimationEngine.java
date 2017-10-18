package space.exploration.mars.rover.animation;

import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.environment.WeatherScanCell;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class WeatherAnimationEngine {
    Properties      marsConfig      = null;
    WeatherScanCell weatherScanCell = null;
    Point           location        = null;
    long            flashSpeed      = 0l;
    JFrame          marsSurface     = null;

    public WeatherAnimationEngine(Properties marsConfig, Point location, long flashSpeed, JFrame marsSurface) {
        this.marsConfig = marsConfig;
        this.location = location;
        this.marsSurface = marsSurface;
        this.flashSpeed = flashSpeed;
    }

    public void updateLocation(Point location){
        this.location = location;
    }

    public void renderWeatherAnimation() {
        JLayeredPane contentPane = AnimationUtil.getContent(marsConfig);
        //contentPane.add(robot, Cell.ROBOT_DEPTH);
        int NUM_FLASHES = 10;
        for (int i = 0; i < NUM_FLASHES; i++) {
            WeatherScanCell weatherScanCell = new WeatherScanCell(marsConfig,location);
            contentPane.add(weatherScanCell, new Integer(Cell.ROBOT_DEPTH + 1));
            marsSurface.setContentPane(contentPane);
            marsSurface.setVisible(true);
            try {
                Thread.sleep(this.flashSpeed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            contentPane.remove(weatherScanCell);

            weatherScanCell.setVisible(false);
            contentPane.add(weatherScanCell, new Integer(Cell.ROBOT_DEPTH + 1));
            marsSurface.setContentPane(contentPane);
            marsSurface.setVisible(true);
            try {
                Thread.sleep(this.flashSpeed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            contentPane.remove(weatherScanCell);
        }
    }
}
