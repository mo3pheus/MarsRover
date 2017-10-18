package space.exploration.mars.rover.animation;

import junit.framework.TestCase;
import org.junit.Test;
import space.exploration.mars.rover.bootstrap.MatrixCreation;
import space.exploration.mars.rover.environment.Cell;
import space.exploration.mars.rover.environment.MarsArchitect;
import space.exploration.mars.rover.environment.WeatherScanCell;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class WeatherAnimationEngineTest extends TestCase {

    private MarsArchitect          marsArchitect;
    private WeatherAnimationEngine weatherAnimationEngine;

    @Override
    public void setUp() {
        try {
            marsArchitect = new MarsArchitect(MatrixCreation.getMatrixConfig());
            weatherAnimationEngine = new WeatherAnimationEngine(MatrixCreation.getMatrixConfig(), marsArchitect
                    .getRobot().getLocation(), 150, marsArchitect.getMarsSurface());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAnimation() throws Exception {
        weatherAnimationEngine.renderWeatherAnimation();
//        WeatherScanCell weatherScanCell = new WeatherScanCell(MatrixCreation.getMatrixConfig(), marsArchitect
//                .getRobot().getLocation());
//        JFrame marsSurface = marsArchitect.getMarsSurface();
//        Container withWeatherCell = marsSurface.getContentPane();
//        withWeatherCell.add(weatherScanCell, Cell.ROBOT_DEPTH+1);
//
//        Container withoutWeatherCell = marsSurface.getContentPane();
//
//        for (int i = 0; i < 10; i++) {
//            marsSurface.setContentPane(withWeatherCell);
//            marsSurface.setVisible(true);
//            Thread.sleep(2000);
//            marsSurface.setContentPane(withoutWeatherCell);
//            marsSurface.setVisible(true);
//        }
    }
}
