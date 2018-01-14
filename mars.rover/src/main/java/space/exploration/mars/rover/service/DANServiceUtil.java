package space.exploration.mars.rover.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DANServiceUtil extends QueryService {
    private Logger logger = LoggerFactory.getLogger(DANServiceUtil.class);
    private int sol;

    public DANServiceUtil(int sol) {
        this.sol = sol;
    }

    @Override
    public final String getQueryString() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("http://pds-geosciences.wustl.edu/msl/msl-m-dan-3_4-rdr-v1/msldan_1xxx/data" +
                            convertSolToString());
        return queryBuilder.toString();
    }


    public final String getTargetURL() {
        try {
            Document responseDoc = Jsoup.connect(getQueryString()).get();
            for (Element element : responseDoc.select("a[href]")) {
                if (element.text().contains("rpa") && element.text().contains("dat")) {
                    return (getQueryString() + element.text());
                }
            }
        } catch (Exception e) {
            logger.error(" DAN Config Util - could not find data files for sol = " + sol + " url = " + getQueryString
                    ());
        }
        return null;
    }

    private final String convertSolToString() {
        int slots  = 4;
        int digits = Integer.toString(sol).length();

        String convertedInt = "/sol";
        for (int i = 0; i < (slots - digits); i++) {
            convertedInt += "0";
        }
        convertedInt += Integer.toString(sol) + "/";
        return convertedInt;
    }

}