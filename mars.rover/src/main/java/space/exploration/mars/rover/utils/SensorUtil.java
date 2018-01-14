package space.exploration.mars.rover.utils;

import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.service.DanRDRData;
import space.exploration.mars.rover.sensor.DANSpectrometer;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class SensorUtil {
    public static Logger logger = LoggerFactory.getLogger(SensorUtil.class);

    public static DanRDRData.DANDerivedData getClosestDanRecord(List<DanRDRData.DANDerivedData> danDerivedDataList,
                                                                long sclkCalTime) {
        org.joda.time.format.DateTimeFormatter formatter =
                DateTimeFormat.forPattern(DANSpectrometer.DAN_UTC_FORMAT);
        DanRDRData.DANDerivedData closestMatch = null;
        long                      timeDiff     = Long.MAX_VALUE;
        for (DanRDRData.DANDerivedData danDerivedData : danDerivedDataList) {
            String utcTimeStamp = danDerivedData.getUtcTimeStamp();
            utcTimeStamp = utcTimeStamp.replaceAll("T", "~");
            long candidateTime = formatter.parseMillis(utcTimeStamp);
            if (timeDiff > Math.abs(sclkCalTime - candidateTime)) {
                closestMatch = danDerivedData;
                timeDiff = Math.abs(sclkCalTime - candidateTime);
            }
        }
        logger.info("Closest match was " + TimeUnit.MILLISECONDS.toMinutes(timeDiff) + " minutes away.");

        return closestMatch;
    }
}
