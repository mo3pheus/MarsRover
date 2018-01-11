package space.exploration.mars.rover.service;

import junit.framework.TestCase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.kernel.SpacecraftClock;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class DANServiceTest extends TestCase {
    private             DANService danService = null;
    public static final String     SEPARATOR  = "-------------------------------------------------------------------";
    Logger logger = LoggerFactory.getLogger(SpacecraftClock.class);

    @Override
    public void setUp() {
        danService = new DANService(1204);
        danService.executeQuery();
        System.out.println(SEPARATOR);
        System.out.println("Running DAN Calibration Test.");
        System.out.println(SEPARATOR);
    }

    @Test
    public void testDanService() throws IOException {
        System.out.println(danService.getResponseAsString());
        Document responseDoc = Jsoup.connect(danService.getQueryString()).get();
        Elements links       = responseDoc.select("a[href]");

        Set<String> dataFile = new HashSet<>();
        for( Element element: links){
            if(element.text().contains("rpa") && element.text().contains("dat")){
                dataFile.add(element.text());
            }
        }

        for(String url:dataFile){
            System.out.println(url);
        }

        System.out.println(SEPARATOR);
        System.out.println("End of DAN Calibration test!");
        System.out.println(SEPARATOR);
    }
}
