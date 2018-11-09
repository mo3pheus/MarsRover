package space.exploration.mars.rover.kernel;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.spice.MSLRelativePositions;
import space.exploration.mars.rover.bootstrap.MarsMissionLaunch;
import space.exploration.mars.rover.bootstrap.MatrixCreation;

import java.util.List;
import java.util.Properties;

public class PositionTestDriver {

    static Logger logger = LoggerFactory.getLogger(PositionTestDriver.class);

    public static void main(String[] args) throws Exception {
        MarsMissionLaunch.configureConsoleLogging(false);
        Properties             roverConfig            = MatrixCreation.getConfig();
        PositionSensorDataUtil positionSensorDataUtil = new PositionSensorDataUtil(roverConfig);
        List<Long> coverageGaps = positionSensorDataUtil
                .getCoverageData("2016-03-17~11:00:00", "2016-03-17~13:00:00");
        for (String timestamp : positionSensorDataUtil.getFormattedCoverageGaps()) {
            logger.info("Coverage gap at " + timestamp);
        }

        for (Long timestamp : coverageGaps) {
            MSLRelativePositions.MSLRelPositionsPacket[] rangingData = positionSensorDataUtil
                    .getNearestValidData(timestamp);
            logger.info("Coverage gap timestamp = " + timestamp + " validRange pulledout = " + rangingData.length);
            logger.info("***************************************************");
            for (MSLRelativePositions.MSLRelPositionsPacket positionsPacket : rangingData) {
                logger.info(positionsPacket.toString());
            }
        }
    }
}
