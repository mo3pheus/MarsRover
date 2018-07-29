package space.exploration.mars.rover.service.calibration.spectrometer.apxs;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.sensors.apxs.ApxsData;
import space.exploration.mars.rover.utils.ServiceUtil;

import java.io.IOException;

public class ApxsDataService {
    private String                  fileUrl         = "http://pds-geosciences.wustl" +
            ".edu/msl/msl-m-apxs-4_5-rdr-v1/mslapx_1xxx/data/sol";
    private int                     sol             = 0;
    private Logger                  logger          = LoggerFactory.getLogger(ApxsDataService.class);
    private ApxsData.ApxsDataPacket apxsDataPacket  = null;
    private String                  archiveLocation = null;

    public ApxsDataService(int sol, String archiveLocation) throws IOException {
        this.sol = sol;
        fileUrl += getSolString();
        fileUrl = getTargetURL();
        this.archiveLocation = archiveLocation;
        apxsDataPacket = ServiceUtil.extractApxsData(ServiceUtil.downloadCsv(fileUrl, archiveLocation + ("apxsData_"
                + Integer.toString(sol) + ".data")));
        ApxsData.ApxsDataPacket.Builder apxsBuilder = ApxsData.ApxsDataPacket.newBuilder().mergeFrom
                (apxsDataPacket).setSol(sol);
        apxsDataPacket = apxsBuilder.build();
        logger.info("ApxsSpectrometer configured successfully for sol = " + sol);
    }

    public ApxsData.ApxsDataPacket getApxsDataPacket() {
        return apxsDataPacket;
    }

    private final String getSolString() {
        String solAsString = Integer.toString(sol);
        int    numZeros    = 5 - solAsString.length();
        String solString   = "";
        for (int i = 0; i < numZeros; i++) {
            solString += "0";
        }
        solString += solAsString + "/";
        return solString;
    }

    private final String getTargetURL() {
        try {
            Document responseDoc = Jsoup.connect(fileUrl).get();
            for (Element element : responseDoc.select("a[href]")) {
                if (element.text().contains("rwp") && element.text().contains("csv")) {
                    return (fileUrl + element.text());
                }
            }
        } catch (Exception e) {
            logger.error(" Could not find data files for sol = " + sol + " url = " + fileUrl, e);
        }
        return null;
    }

}
