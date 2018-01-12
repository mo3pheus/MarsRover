package space.exploration.mars.rover.service;

import junit.framework.TestCase;
import org.junit.Test;
import space.exploration.communications.protocol.service.DanRDRData;

import java.io.IOException;

public class DanCalibrationTest extends TestCase{
    private DANCalibrationService danCalibrationService;

    @Override
    public void setUp(){
        danCalibrationService = new DANCalibrationService(1297);
    }

    @Test
    public void testFieldRead() throws IOException {
        for(DanRDRData.DANDerivedData danDerivedData: danCalibrationService.getDanPayload()){
            System.out.println("=============================================================");
            System.out.println(danDerivedData);
            System.out.println("=============================================================");
        }
    }

}
