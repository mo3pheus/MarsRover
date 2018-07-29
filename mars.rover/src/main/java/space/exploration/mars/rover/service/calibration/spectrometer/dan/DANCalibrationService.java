package space.exploration.mars.rover.service.calibration.spectrometer.dan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.service.DanRDRData;
import space.exploration.mars.rover.utils.ServiceUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DANCalibrationService {
    /* Please refer to dan_rdr_derived_passiv.fmt for format details */
    public static final int[]  BYTE_SCHEMA = {4, 23, 4, 4, 4, 4, 4, 4, 4, 4, 4, 8};
    private             Logger logger      = LoggerFactory.getLogger(DANCalibrationService.class);

    private DANServiceUtil danServiceUtil = null;

    public DANCalibrationService(int sol) {
        danServiceUtil = new DANServiceUtil(sol);
    }

    public List<DanRDRData.DANDerivedData> getDanPayload() throws IOException {
        List<DanRDRData.DANDerivedData> danPayload = new ArrayList<>();
        String                          url        = danServiceUtil.getTargetURL();
        byte[]                          content    = ServiceUtil.download(url);

        int numRows = (int) (content.length / 71);
        for (int i = 0; i < numRows; i++) {
            danPayload.add(parseDanData(readDataRow(content, (i * 71))));
        }

        return danPayload;
    }

    private Map<Integer, byte[]> readDataRow(byte[] content, int offset) {
        Map<Integer, byte[]> fields = new HashMap<>();
        if (content != null) {
            int i = offset;
            for (int j = 0; j < BYTE_SCHEMA.length; j++) {
                byte[] fieldBytes = new byte[BYTE_SCHEMA[j]];
                for (int k = 0; k < BYTE_SCHEMA[j]; k++) {
                    fieldBytes[k] = content[i];
                    i++;
                }
                fields.put(j, fieldBytes);
            }
        }
        return fields;
    }

    private DanRDRData.DANDerivedData parseDanData(Map<Integer, byte[]> fields) {
        DanRDRData.DANDerivedData.Builder dBuilder = DanRDRData.DANDerivedData.newBuilder();

        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);
        byteBuffer.put(fields.get(0));
        byteBuffer.flip();
        dBuilder.setInstrumentTimeMillis(byteBuffer.getInt());

        byte[] tempUTC = fields.get(1);
        dBuilder.setUtcTimeStamp(new String(tempUTC));

        byteBuffer = ByteBuffer.allocate(Float.BYTES);
        byteBuffer.put(fields.get(2));
        byteBuffer.flip();
        dBuilder.setBeginLatitude(byteBuffer.getFloat());

        byteBuffer = ByteBuffer.allocate(Float.BYTES);
        byteBuffer.put(fields.get(3));
        byteBuffer.flip();
        dBuilder.setBeginLongitude(byteBuffer.getFloat());

        byteBuffer = ByteBuffer.allocate(Float.BYTES);
        byteBuffer.put(fields.get(4));
        byteBuffer.flip();
        dBuilder.setEndLatitude(byteBuffer.getFloat());

        byteBuffer = ByteBuffer.allocate(Float.BYTES);
        byteBuffer.put(fields.get(5));
        byteBuffer.flip();
        dBuilder.setEndLongitude(byteBuffer.getFloat());

        byteBuffer = ByteBuffer.allocate(Float.BYTES);
        byteBuffer.put(fields.get(6));
        byteBuffer.flip();
        dBuilder.setCollectionDurationSeconds(byteBuffer.getFloat());

        byteBuffer = ByteBuffer.allocate(Float.BYTES);
        byteBuffer.put(fields.get(7));
        byteBuffer.flip();
        dBuilder.setCtnBackgroundCounts(byteBuffer.getFloat());

        byteBuffer = ByteBuffer.allocate(Float.BYTES);
        byteBuffer.put(fields.get(8));
        byteBuffer.flip();
        dBuilder.setCtnCounts(byteBuffer.getFloat());

        byteBuffer = ByteBuffer.allocate(Float.BYTES);
        byteBuffer.put(fields.get(9));
        byteBuffer.flip();
        dBuilder.setCetnBackgroundCounts(byteBuffer.getFloat());

        byteBuffer = ByteBuffer.allocate(Float.BYTES);
        byteBuffer.put(fields.get(10));
        byteBuffer.flip();
        dBuilder.setCetnCounts(byteBuffer.getFloat());

        dBuilder.setLocalSolarTime(new String(fields.get(11)));

        DanRDRData.DANDerivedData danDerivedData = dBuilder.build();
        return danDerivedData;
    }

}
