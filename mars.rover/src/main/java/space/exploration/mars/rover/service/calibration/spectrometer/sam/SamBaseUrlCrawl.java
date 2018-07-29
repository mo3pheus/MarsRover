package space.exploration.mars.rover.service.calibration.spectrometer.sam;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import space.exploration.mars.rover.sensor.SamSensor;
import space.exploration.mars.rover.service.QueryService;

import java.util.ArrayList;
import java.util.List;

public class SamBaseUrlCrawl extends QueryService {

    @Override
    public String getQueryString() {
        return SamSensor.SAM_DATA_BASE_URL;
    }

    public List<String> getExperimentIds() {
        List<String> expIds = new ArrayList<>();
        try {
            Document responseDoc = Jsoup.connect(getQueryString()).get();
            for (Element element : responseDoc.select("a[href]")) {
                if (element.text().contains("eid")) {
                    expIds.add(element.text());
                }
            }
        } catch (Exception e) {
            logger.error(" Sam Base Url crawl error.");
        }
        return expIds;
    }
}
