package space.exploration.mars.rover.kernel;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.spice.MSLRelativePositions;
import space.exploration.spice.utilities.PositionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class PositionSensorDataUtil {
    private PositionUtils                                    positionUtils    = null;
    private DateTime                                         startTime        = null;
    private DateTime                                         endTime          = null;
    private Logger                                           logger           = LoggerFactory.getLogger(PositionSensorDataUtil.class);
    private List<MSLRelativePositions.MSLRelPositionsPacket> positionsPackets = new ArrayList<>();

    public PositionSensorDataUtil(Properties roverConfig) {
        positionUtils = new PositionUtils(roverConfig.getProperty("mars.rover.spice.positionUtilFile"));
    }

    public void getCoverageData(String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd~HH:mm:ss");
        startTime = formatter.parseDateTime(startDate);
        endTime = formatter.parseDateTime(endDate);

        while ( startTime.getMillis() < endTime.getMillis() ) {
            positionUtils.setUtcTime(formatter.print(startTime));
            try {
                MSLRelativePositions.MSLRelPositionsPacket positionsPacket = positionUtils.getPositionPacket();
                logger.info("Positions Packet at " + formatter.print(startTime) + " => " + positionsPacket.toString());
                positionsPackets.add(positionsPacket);
                //Thread.sleep(1000l);
            } catch (Exception e) {
                logger.error("Coverage gap at " + formatter.print(startTime));
            }
            //startTime = new DateTime(startTime.getMillis() + TimeUnit.HOURS.toMillis(1l));
            startTime = new DateTime(startTime.getMillis() + TimeUnit.MINUTES.toMillis(10l));
        }

    }
}
