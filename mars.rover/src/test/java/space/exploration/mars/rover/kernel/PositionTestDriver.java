package space.exploration.mars.rover.kernel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.bootstrap.MarsMissionLaunch;
import space.exploration.mars.rover.bootstrap.MatrixCreation;
import space.exploration.mars.rover.utils.PositionPredictor;

import java.util.List;
import java.util.Properties;

public class PositionTestDriver {

    static Logger logger = LoggerFactory.getLogger(PositionTestDriver.class);

    public static void main(String[] args) throws Exception {
        MarsMissionLaunch.configureConsoleLogging(false);
        Properties        roverConfig       = MatrixCreation.getConfig();
        PositionPredictor positionPredictor = new PositionPredictor(roverConfig);

        List<Long> coverageGaps = positionPredictor
                .getCoverageData("2016-03-17~11:00:00", "2016-03-17~13:00:00");
        for (String timestamp : positionPredictor.getFormattedCoverageGaps()) {
            logger.info("Coverage gap at " + timestamp);
        }

//        for (Long timestamp : coverageGaps) {
//            Map<Long, MSLRelativePositions.MSLRelPositionsPacket> positionsMap = positionPredictor
//                    .getNearestValidData(timestamp);
//            for (Long ts : positionsMap.keySet()) {
//                logger.info("Position at " + ts + " => " + positionsMap.get(ts));
//            }
//            logger.info("Coverage gap timestamp = " + timestamp + " validRange pulledout = " + positionsMap.size());
//            logger.info("***************************************************");
//        }

        for (Long timestamp : coverageGaps) {
            logger.info("Predicted position at ts = " + timestamp + " ==> " + positionPredictor
                    .getPredictedPosition(timestamp));
        }
    }
}
