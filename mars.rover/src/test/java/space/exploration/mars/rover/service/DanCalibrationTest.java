package space.exploration.mars.rover.service;

import junit.framework.TestCase;
import org.junit.Test;
import space.exploration.communications.protocol.service.DanRDRData;
import space.exploration.mars.rover.service.calibration.spectrometer.dan.DANCalibrationService;

import java.io.IOException;
import java.util.List;

public class DanCalibrationTest extends TestCase {
    private DANCalibrationService danCalibrationService;

    @Override
    public void setUp() {
        danCalibrationService = new DANCalibrationService(1297);
    }

    @Test
    public void testFieldRead() throws IOException, InterruptedException {
        List<DanRDRData.DANDerivedData> danPayload = danCalibrationService.getDanPayload();
        for (DanRDRData.DANDerivedData danDerivedData : danPayload) {
            System.out.println("=============================================================");
            System.out.println(danDerivedData);
            System.out.println("=============================================================");
            Thread.sleep(10);
        }
        assertEquals(198, danPayload.size());
    }
}
