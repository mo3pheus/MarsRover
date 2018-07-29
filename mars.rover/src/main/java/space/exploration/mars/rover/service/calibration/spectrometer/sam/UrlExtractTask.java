package space.exploration.mars.rover.service.calibration.spectrometer.sam;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.sensor.SamSensor;

import java.util.List;
import java.util.concurrent.*;

public class UrlExtractTask implements Callable<DataAvailabilityPacket> {
    private String              experimentId = null;
    private Logger              logger       = LoggerFactory.getLogger(UrlExtractTask.class);
    private CloseableHttpClient httpClient   = null;
    private HttpGet             httpGet      = null;

    @Override
    public DataAvailabilityPacket call() throws Exception {
        Thread.currentThread().setName("samUrlExtractionTask" + experimentId);
        DataAvailabilityPacket dataAvailabilityPacket = null;
        try {
            logger.info("Started httpRequest.");
            dataAvailabilityPacket = getDataAvailability();
            logger.info(dataAvailabilityPacket.toString());
            logger.info("Finished httpRequest.");
        } catch (Exception e) {
            logger.error("SAM data unavailable for experiment id = " + experimentId + e.getMessage(), e);
        } finally {
            httpGet.releaseConnection();
        }

        return dataAvailabilityPacket;
    }

    public UrlExtractTask(CloseableHttpClient httpClient, String experimentId, RequestConfig requestConfig) {
        this.httpClient = httpClient;
        this.experimentId = experimentId;
        httpGet = new HttpGet(getQueryString());
        httpGet.setConfig(requestConfig);
    }

    public String getQueryString() {
        return SamSensor.SAM_DATA_BASE_URL + experimentId + "/level2";
    }

    public DataAvailabilityPacket getDataAvailability1() throws Exception {
        DataAvailabilityPacket dataAvailabilityPacket = new DataAvailabilityPacket();

        Document responseDoc = Jsoup.connect(getQueryString()).get();
        for (Element element : responseDoc.select("a[href]")) {
            if (element.text().contains("csv")) {
                dataAvailabilityPacket.addUrl(element.text());
            }
        }

        dataAvailabilityPacket.setSol(extractSol(dataAvailabilityPacket.getUrls()));
        dataAvailabilityPacket.setExperimentId(experimentId);
        return dataAvailabilityPacket;
    }

    public DataAvailabilityPacket getDataAvailability() throws Exception {
        DataAvailabilityPacket dataAvailabilityPacket = new DataAvailabilityPacket();

        HttpResponse httpResponse = httpClient.execute(httpGet);
        Document responseDoc = Jsoup.parse(httpResponse.getEntity().getContent(), "UTF-8", SamSensor
                .SAM_DATA_BASE_URL);
        for (Element element : responseDoc.select("a[href]")) {
            if (element.text().contains("csv")) {
                dataAvailabilityPacket.addUrl(element.text());
            }
        }

        dataAvailabilityPacket.setSol(extractSol(dataAvailabilityPacket.getUrls()));
        dataAvailabilityPacket.setExperimentId(experimentId);
        return dataAvailabilityPacket;
    }

    private int extractSol(List<String> urls) {
        int sol = 0;

        for (String url : urls) {
            if (url.contains(".csv")) {
                sol = Integer.parseInt(url.substring(8, 12));
                break;
            }
        }

        return sol;
    }
}

