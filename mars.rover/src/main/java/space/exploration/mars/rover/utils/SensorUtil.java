package space.exploration.mars.rover.utils;

import org.joda.time.format.DateTimeFormat;
import space.exploration.communications.protocol.service.DanRDRData;
import space.exploration.mars.rover.sensor.DANSpectrometer;

import java.util.List;

public class SensorUtil {
    public static DanRDRData.DANDerivedData getClosestDanRecord(List<DanRDRData.DANDerivedData> danDerivedDataList,
                                                                long ephemerisTime) {
        org.joda.time.format.DateTimeFormatter formatter =
                DateTimeFormat.forPattern(DANSpectrometer.DAN_UTC_FORMAT);
        DanRDRData.DANDerivedData closestMatch = null;
        long                      timeDiff     = Long.MAX_VALUE;
        for (DanRDRData.DANDerivedData danDerivedData : danDerivedDataList) {
            String utcTimeStamp  = danDerivedData.getUtcTimeStamp();
            long   candidateTime = formatter.parseMillis(utcTimeStamp);
            if (timeDiff < Math.abs(ephemerisTime - candidateTime)) {
                closestMatch = danDerivedData;
                timeDiff = Math.abs(ephemerisTime - candidateTime);
            }
        }

        return closestMatch;
    }
}
