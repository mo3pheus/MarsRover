package space.exploration.mars.rover.kernel;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
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
    List<Long>                                            timestampList      = new ArrayList<>();
    Map<Long, MSLRelativePositions.MSLRelPositionsPacket> positionsPacketMap = new HashMap<>();

    DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd~HH:mm:ss");

    public PositionSensorDataUtil(Properties roverConfig) {
        positionUtils = new PositionUtils(roverConfig.getProperty("mars.rover.spice.positionUtilFile"));
    }

    public List<Long> getCoverageData(String startDate, String endDate) {
        startTime = formatter.parseDateTime(startDate);
        endTime = formatter.parseDateTime(endDate);

        while ( startTime.getMillis() < endTime.getMillis() ) {
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

    protected Map<Long, MSLRelativePositions.MSLRelPositionsPacket> getNearestValidData(Long timestamp) throws IllegalArgumentException {
        int i                  = 0;
        int preTimestampCount  = 0;
        int postTimestampCount = 0;
        int numAttempts        = 0;

        Long currentTimestamp = timestamp - TimeUnit.MINUTES.toMillis(1l);

        while ( preTimestampCount < 100 ) {
            if ( numAttempts++ > MAX_ATTEMPTS ) {
                throw new IllegalArgumentException("Can not find valid range for timestamp = " + timestamp);
            }

            try {
                extractPositionData(currentTimestamp);
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
        while ( postTimestampCount < 50 ) {
            if ( numAttempts++ > MAX_ATTEMPTS ) {
                throw new IllegalArgumentException("Can not find valid range for timestamp = " + timestamp);
            }

            try {
                extractPositionData(currentTimestamp);
                postTimestampCount++;
            } catch (Exception e) {
                logger.debug("Gap at timestamp = " + currentTimestamp, e);
                logger.info(
                        "NumAttempts = " + numAttempts + " validCount so far = " + preTimestampCount + " currentTS = "
                                + formatter
                                .print(currentTimestamp));
            }
            currentTimestamp += TimeUnit.MINUTES.toMillis(1l);
        }

        return positionsPacketMap;
    }

    private void extractPositionData(Long timestamp) throws Exception {
        positionUtils.setUtcTime(formatter.print(timestamp));
        MSLRelativePositions.MSLRelPositionsPacket positionsPacket = positionUtils.getPositionPacket();
        timestampList.add(timestamp);
        positionsPacketMap.put(timestamp, positionsPacket);
    }

    public MSLRelativePositions.MSLRelPositionsPacket getPredictedPosition(Long timestamp) {
        double[] timestamps = new double[positionsPacketMap.keySet().size()];
        double[] x          = new double[positionsPacketMap.keySet().size()];
        double[] y          = new double[positionsPacketMap.keySet().size()];
        double[] z          = new double[positionsPacketMap.keySet().size()];
        double[] vx         = new double[positionsPacketMap.keySet().size()];
        double[] vy         = new double[positionsPacketMap.keySet().size()];
        double[] vz         = new double[positionsPacketMap.keySet().size()];
        double[] angSep     = new double[positionsPacketMap.keySet().size()];

        int i = 0;
        for (Long ts : positionsPacketMap.keySet()) {
            MSLRelativePositions.MSLRelPositionsPacket packet = positionsPacketMap.get(ts);
            timestamps[i] = (double) ts;
            x[i] = packet.getStateCuriosity(0);
            y[i] = packet.getStateCuriosity(1);
            z[i] = packet.getStateCuriosity(2);
            vx[i] = packet.getStateCuriosity(3);
            vy[i] = packet.getStateCuriosity(4);
            vz[i] = packet.getStateCuriosity(5);
            angSep[i] = packet.getAngSepHGAEarth();
            i++;
        }

        SplineInterpolator[]       positionPredictor         = new SplineInterpolator[7];
        PolynomialSplineFunction[] polynomialSplineFunctions = new PolynomialSplineFunction[7];
        for (int j = 0; j < 7; j++) {
            positionPredictor[j] = new SplineInterpolator();
        }

        polynomialSplineFunctions[0] = positionPredictor[0].interpolate(timestamps, x);
        polynomialSplineFunctions[1] = positionPredictor[1].interpolate(timestamps, y);
        polynomialSplineFunctions[2] = positionPredictor[2].interpolate(timestamps, z);
        polynomialSplineFunctions[3] = positionPredictor[3].interpolate(timestamps, vx);
        polynomialSplineFunctions[4] = positionPredictor[4].interpolate(timestamps, vy);
        polynomialSplineFunctions[5] = positionPredictor[5].interpolate(timestamps, vz);
        polynomialSplineFunctions[6] = positionPredictor[6].interpolate(timestamps, angSep);

        MSLRelativePositions.MSLRelPositionsPacket.Builder mBuilder = MSLRelativePositions.MSLRelPositionsPacket.newBuilder();
        for (int k = 0; k < 6; k++) {
            mBuilder.setStateCuriosity(k, polynomialSplineFunctions[k].value(timestamp));
        }
        mBuilder.setAngSepHGAEarth(polynomialSplineFunctions[6].value(timestamp));

        return mBuilder.build();
    }
}
