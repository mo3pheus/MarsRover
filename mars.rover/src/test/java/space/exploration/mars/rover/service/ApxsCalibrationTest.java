package space.exploration.mars.rover.service;

import junit.framework.TestCase;
import org.junit.Test;
import space.exploration.mars.rover.sensors.apxs.ApxsData;
import space.exploration.mars.rover.utils.ServiceUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ApxsCalibrationTest extends TestCase {
    public static final String PRETTY_STRING = "==================================================================";

    @Override
    public void setUp() {
        System.out.println(PRETTY_STRING);
        System.out.println("Running ApxsCalibrationTest");
        System.out.println(PRETTY_STRING);
    }

    @Test
    public void testApxsRead() {
        String url = "http://pds-geosciences.wustl" +
                ".edu/msl/msl-m-apxs-4_5-rdr-v1/mslapx_1xxx/data/sol01444/apb_525711448rwp14440571020_______p1.csv";
        try {
            File apxsFile = ServiceUtil.downloadCsv(url, "testApxs.txt");
            assertNotNull(apxsFile);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(apxsFile));
            String         dataLine       = null;
            while ((dataLine = bufferedReader.readLine()) != null) {
                System.out.println(dataLine);
            }

            System.out.println(PRETTY_STRING);
            ApxsData.ApxsDataPacket apxsDataPacket = ServiceUtil.extractApxsData(apxsFile);
            assertNotNull(apxsDataPacket);
            assertEquals(1, 1);

            double totalPercentage = 0.0d;
            for (ApxsData.ApxsDataPacket.apxsElement apxsElement : apxsDataPacket.getElementCompositionList()) {
                totalPercentage += apxsElement.getPercentage();
            }
            assertTrue(totalPercentage > 99.9d);

            System.out.println(apxsDataPacket);
            System.out.println(PRETTY_STRING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUrlExtraction() {
        ApxsDataService apxsDataService = null;
        try {
            apxsDataService = new ApxsDataService(1294);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ApxsData.ApxsDataPacket apxsDataPacket = apxsDataService.getApxsDataPacket();
        assertNotNull(apxsDataPacket);
        assertEquals(1294, apxsDataPacket.getSol());

        double totalPercentage = 0.0d;
        for (ApxsData.ApxsDataPacket.apxsElement apxsElement : apxsDataPacket.getElementCompositionList()) {
            totalPercentage += apxsElement.getPercentage();
        }
        assertTrue(totalPercentage > 99.9d);

        System.out.println(apxsDataPacket);
        System.out.println(PRETTY_STRING);
    }

    @Test
    public void testInvalidUrl() throws IOException {
        boolean exceptionThrown = false;
        try {
            ApxsDataService apxsDataService = new ApxsDataService(-122);
        } catch (IOException io) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }
}
