package space.exploration.mars.rover.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApxsDataService {
    private String fileUrl = "http://pds-geosciences.wustl.edu/msl/msl-m-apxs-4_5-rdr-v1/mslapx_1xxx/data/sol";
    private int    sol     = 0;
    private Logger logger  = LoggerFactory.getLogger(ApxsDataService.class);

    public ApxsDataService(int sol) {
        this.sol = sol;
        fileUrl += getSolString();
        fileUrl = getTargetURL();
    }


    private final String getSolString() {
        String solAsString = Integer.toString(sol);
        int    numZeros    = 5 - solAsString.length();
        String solString   = "";
        for (int i = 0; i < numZeros; i++) {
            solString += "0";
        }
        solString += solAsString;
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
