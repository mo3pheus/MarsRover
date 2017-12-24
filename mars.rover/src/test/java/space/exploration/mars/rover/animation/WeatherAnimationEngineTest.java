package space.exploration.mars.rover.animation;

import junit.framework.TestCase;
import org.junit.Test;
import space.exploration.mars.rover.bootstrap.MatrixCreation;
import space.exploration.mars.rover.environment.MarsArchitect;

import java.io.IOException;

public class WeatherAnimationEngineTest extends TestCase {

    private MarsArchitect          marsArchitect;
    private WeatherAnimationEngine weatherAnimationEngine;

    @Override
    public void setUp() {
        try {
            marsArchitect = new MarsArchitect(MatrixCreation.getConfig());
            weatherAnimationEngine = new WeatherAnimationEngine(MatrixCreation.getConfig(), marsArchitect
                    .getRobot().getLocation(), 150, marsArchitect.getMarsSurface());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAnimation() throws Exception {
        weatherAnimationEngine.renderWeatherAnimation();
    }
}
