package space.exploration.mars.rover.sensor.sam;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import space.exploration.mars.rover.bootstrap.MarsMissionLaunch;
import space.exploration.mars.rover.bootstrap.MatrixCreation;
import space.exploration.mars.rover.sensor.SamSensor;
import space.exploration.mars.rover.service.calibration.spectrometer.sam.DataAvailabilityPacket;

import space.exploration.mars.rover.utils.ServiceUtil;
import util.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class SamSensorTest extends TestCase {

    SamSensor              samSensor              = null;
    Properties             marsConfig             = null;
    DataAvailabilityPacket dataAvailabilityPacket = null;

    @Before
    public void setUp() {
        try {
            MarsMissionLaunch.configureConsoleLogging(false);
            marsConfig = MatrixCreation.getConfig();
            samSensor  = new SamSensor(marsConfig);
            FileUtil.processDirectories("dataArchives/SAM/");
            FileUtil.processDirectories("dataArchives/SAM/test/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSamDataAvailability() {
        System.out.println(samSensor.getDataAvailabilityPacketMap().get(1225).toString());
        dataAvailabilityPacket = samSensor.getDataAvailabilityPacketMap().get(1225);
        assertEquals(1225, dataAvailabilityPacket.getSol());
        assertEquals("eid25327", dataAvailabilityPacket.getExperimentId());
        assertTrue(dataAvailabilityPacket.getUrls().contains("sm25327f1225rdr2__spyr_qms_egacomp_1.csv"));
        assertTrue(dataAvailabilityPacket.getUrls().contains("sm25327f1225rdr2__spyr_qms_egaxxxx_1.csv"));
    }

    @Test
    public void testFileDownload() {
        dataAvailabilityPacket = samSensor.getDataAvailabilityPacketMap().get(1225);
        String fileUrl = SamSensor.SAM_DATA_BASE_URL + dataAvailabilityPacket.getExperimentId() +
                "/level2/sm25327f1225rdr2__spyr_qms_egacomp_1.csv";
        try {
            String path = "dataArchives/SAM/test/" + Integer.toString(dataAvailabilityPacket.getSol());
            FileUtil.processDirectories(path);
            path += "/1225SamData.csv";
            File           csvFile        = ServiceUtil.downloadCsv(fileUrl, path);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFile));
            String         line           = null;

            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                System.out.println("==================================");
                String[] parts     = line.split(",");
                Double   abundance = 0.0d;
                try {
                    abundance = Double.parseDouble(parts[parts.length - 1]);
                } catch (NumberFormatException nfe) {
                    continue;
                }
                int startQuote = line.indexOf("\"");
                int endQuote   = line.lastIndexOf("\"");
                System.out.println(line.substring(startQuote + 1, endQuote));
                System.out.println(abundance);
                System.out.println("==================================");
            }
            bufferedReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
