package space.exploration.mars.rover.service;

import junit.framework.TestCase;
import org.junit.Test;
import space.exploration.mars.rover.service.calibration.spectrometer.dan.DANCalibrationService;
import space.exploration.mars.rover.utils.ServiceUtil;

import java.io.File;
import java.io.IOException;

public class ByteConversionsTest extends TestCase {
    private String fileName = "dna_418793176rpa02400060000_______p1.dat";
    private File   file     = null;

    @Test
    public void testByteConversion() throws IOException {
        byte[] contents = ServiceUtil.download("http://pds-geosciences.wustl" +
                                                       ".edu/msl/msl-m-dan-3_4-rdr-v1/msldan_1xxx/data/sol0240/" +
                                                       fileName);
        if(contents == null){
            System.out.println("No File");
        } else{
            System.out.println(contents.length);
        }

        int rowCount = 0;
        for(int i = 0; i < DANCalibrationService.BYTE_SCHEMA.length; i++){
            rowCount += DANCalibrationService.BYTE_SCHEMA[i];
        }
        System.out.println(rowCount);

        for(int i = 0; i < contents.length; i++){

        }
    }

}
