package space.exploration.mars.rover.kernel;

import space.exploration.mars.rover.bootstrap.MarsMissionLaunch;
import space.exploration.mars.rover.bootstrap.MatrixCreation;

import java.util.Properties;

public class PositionTestDriver {

    public static void main(String[] args) throws Exception {
        MarsMissionLaunch.configureConsoleLogging(false);
        Properties             roverConfig            = MatrixCreation.getConfig();
        PositionSensorDataUtil positionSensorDataUtil = new PositionSensorDataUtil(roverConfig);
        positionSensorDataUtil.getCoverageData("2016-03-17~11:00:00", "2016-03-17~13:00:00");
    }
}
