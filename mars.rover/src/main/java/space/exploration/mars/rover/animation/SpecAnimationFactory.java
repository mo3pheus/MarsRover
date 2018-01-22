package space.exploration.mars.rover.animation;

import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.environment.EnvironmentUtils;

import javax.swing.*;
import java.util.Properties;

public class SpecAnimationFactory {
    private JFrame     marsSurface = null;
    private Properties marsConfig  = null;
    private Cell       robot       = null;

    public SpecAnimationFactory(JFrame marsSurface, Properties marsConfig, Cell robot) {
        this.marsConfig = marsConfig;
        this.marsSurface = marsSurface;
        this.robot = robot;
    }

    public SpecAnimationEngine getSpectrometerAnimationEngine(String spectrometerType) {
        SpecAnimationEngine spectrometerAnimation = new SpecAnimationEngine(marsSurface, robot, marsConfig);
        spectrometerAnimation.setColor(EnvironmentUtils.findColor(spectrometerType));
        return spectrometerAnimation;
    }
}
