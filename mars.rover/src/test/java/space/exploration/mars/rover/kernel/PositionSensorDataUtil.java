package space.exploration.mars.rover.kernel;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.spice.MSLRelativePositions;
import space.exploration.spice.utilities.PositionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PositionSensorDataUtil {
    private final int           MAX_ATTEMPTS          = 1000;
    private       PositionUtils positionUtils         = null;
    private       DateTime      startTime             = null;
    private       DateTime      endTime               = null;
    private       Logger        logger                = LoggerFactory
            .getLogger(PositionSensorDataUtil.class);
    private       List<Long>    coverageGaps          = new ArrayList<>();
    private       List<String>  formattedCoverageGaps = new ArrayList<>();

    DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd~HH:mm:ss");

    public PositionSensorDataUtil(Properties roverConfig) {
        positionUtils = new PositionUtils(roverConfig.getProperty("mars.rover.spice.positionUtilFile"));
    }

    public List<Long> getCoverageData(String startDate, String endDate) {
        startTime = formatter.parseDateTime(startDate);
        endTime = formatter.parseDateTime(endDate);

        while (startTime.getMillis() < endTime.getMillis()) {
            positionUtils.setUtcTime(formatter.print(startTime));
            try {
                MSLRelativePositions.MSLRelPositionsPacket positionsPacket = positionUtils.getPositionPacket();
            } catch (Exception e) {
                logger.error("Coverage gap at " + formatter.print(startTime));
                formattedCoverageGaps.add(formatter.print(startTime.getMillis()));
                coverageGaps.add(startTime.getMillis());
            }
            startTime = new DateTime(startTime.getMillis() + TimeUnit.MINUTES.toMillis(10l));
        }

        return coverageGaps;
    }

    public List<String> getFormattedCoverageGaps() {
        return formattedCoverageGaps;
    }

    protected MSLRelativePositions.MSLRelPositionsPacket[] getNearestValidData(Long timestamp) throws IllegalArgumentException {
        MSLRelativePositions.MSLRelPositionsPacket[] rangingData = new MSLRelativePositions.MSLRelPositionsPacket[150];

        List<Long>                                            timestampList      = new ArrayList<>();
        Map<Long, MSLRelativePositions.MSLRelPositionsPacket> positionsPacketMap = new HashMap<>();

        int i                  = 0;
        int preTimestampCount  = 0;
        int postTimestampCount = 0;
        int numAttempts        = 0;

        Long currentTimestamp = timestamp - TimeUnit.MINUTES.toMillis(1l);

        while (preTimestampCount < 100) {
            if (numAttempts++ > MAX_ATTEMPTS) {
                throw new IllegalArgumentException("Can not find valid range for timestamp = " + timestamp);
            }

            try {
                positionUtils.setUtcTime(formatter.print(currentTimestamp));
                MSLRelativePositions.MSLRelPositionsPacket positionsPacket = positionUtils.getPositionPacket();
                timestampList.add(currentTimestamp);
                positionsPacketMap.put(currentTimestamp, positionsPacket);
                preTimestampCount++;
            } catch (Exception e) {
                logger.debug("Gap at timestamp = " + currentTimestamp, e);
                logger.info(
                        "NumAttempts = " + numAttempts + " validCount so far = " + preTimestampCount + " currentTS = "
                                + formatter
                                .print(currentTimestamp));
            }
            currentTimestamp -= TimeUnit.MINUTES.toMillis(1l);
        }

        currentTimestamp = timestamp + TimeUnit.MINUTES.toMillis(1l);
        while (postTimestampCount < 50) {
            if (numAttempts++ > MAX_ATTEMPTS) {
                throw new IllegalArgumentException("Can not find valid range for timestamp = " + timestamp);
            }

            try {
                positionUtils.setUtcTime(formatter.print(currentTimestamp));
                MSLRelativePositions.MSLRelPositionsPacket positionsPacket = positionUtils.getPositionPacket();
                timestampList.add(currentTimestamp);
                positionsPacketMap.put(currentTimestamp, positionsPacket);
                preTimestampCount++;
            } catch (Exception e) {
                logger.debug("Gap at timestamp = " + currentTimestamp, e);
                logger.info(
                        "NumAttempts = " + numAttempts + " validCount so far = " + preTimestampCount + " currentTS = "
                                + formatter
                                .print(currentTimestamp));
            }
            currentTimestamp += TimeUnit.MINUTES.toMillis(1l);
        }

        Collections.sort(timestampList);
        int index = 0;
        while (index < rangingData.length) {
            rangingData[index] = positionsPacketMap.get(timestampList.get(index));
            index++;
        }

        return rangingData;
    }
}
