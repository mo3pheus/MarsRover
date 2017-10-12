package space.exploration.mars.rover.animation;

import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.environment.WeatherScanCell;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class WeatherAnimationEngine extends CameraAnimationEngine {
    Properties      marsConfig      = null;
    WeatherScanCell weatherScanCell = null;

    public WeatherAnimationEngine(Properties marsConfig, Point location, long shutterSpeed, int cellWidth) {
        super(marsConfig, location, shutterSpeed, cellWidth);
        this.marsConfig = marsConfig;
        this.weatherScanCell = new WeatherScanCell(marsConfig, location);
    }

    public void renderWeatherAnimation() {
        JLayeredPane contentPane = AnimationUtil.getContent(marsConfig);
        contentPane.add(weatherScanCell, Cell.ROBOT_DEPTH);
        int NUM_FLASHES = 10;
        for (int i = 0; i < NUM_FLASHES; i++) {
            weatherScanCell.setLocation(location);
            contentPane.add(weatherScanCell, new Integer(Cell.ROBOT_DEPTH + 1));
            marsSurface.setContentPane(contentPane);
            marsSurface.setVisible(true);
            try {
                Thread.sleep(this.shutterSpeed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            contentPane.remove(weatherScanCell);

            contentPane.add(weatherScanCell, new Integer(Cell.ROBOT_DEPTH + 1));
            marsSurface.setContentPane(contentPane);
            marsSurface.setVisible(true);
            try {
                Thread.sleep(this.shutterSpeed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            contentPane.remove(weatherScanCell);
        }
    }
}
