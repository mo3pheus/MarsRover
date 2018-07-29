package space.exploration.mars.rover.service.calibration.spectrometer.sam;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.bouncycastle.util.encoders.UrlBase64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import space.exploration.mars.rover.bootstrap.MarsMissionLaunch;
import space.exploration.mars.rover.sensor.SamSensor;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
//import space.exploration.mars.rover.bootstrap.MatrixCreation;
//import space.exploration.mars.rover.service.QueryService;
//
//import java.util.List;

public class SamCalibrationUtil {
    public enum CALIBRATION_STATUS_CODE {
        INIT,
        IN_PROGRESS,
        ERROR,
        SUCCESS
    }

    //    public static List<String> getExperimentIds(){
//        SamBaseUrlCrawl crawl = new SamBaseUrlCrawl();
//        crawl.executeQuery();
//        System.out.println(crawl.getExperimentIds());
//        return null;
//    }
//
    public static void main(String[] args) throws Exception {

        MarsMissionLaunch.configureLogging(false);
//        ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
//        CloseableHttpClient httpClient = getDefaultHttpClient();
//        UrlExtractTask urlExtractTask = new UrlExtractTask(httpClient, "eid25188",
//                                                           getDefaultRequestConfig());
//        executorService.submit(urlExtractTask);
//        executorService.shutdown();
//        System.out.println(urlExtractTask.getDataAvailability());
//        httpClient.close();

        SamSensor samSensor = new SamSensor(null);
        if (samSensor.isCalibrated()) {
            System.out.println(samSensor);
        }
        //testUrlHttpClient(SamSensor.SAM_DATA_BASE_URL + "eid25223/level2");
    }


    public static void testUrlHttpClient(String url) {
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new
                PoolingHttpClientConnectionManager();
        poolingHttpClientConnectionManager.setMaxTotal(1);

        CloseableHttpClient closeableHttpClient = HttpClients.custom().setConnectionManager
                (poolingHttpClientConnectionManager).build();

        HttpGet httpGet = new HttpGet(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(10000).setConnectTimeout
                (10000).setSocketTimeout(10000).setMaxRedirects(3).build();
        httpGet.setConfig(requestConfig);

        try {
            HttpResponse httpResponse = closeableHttpClient.execute(httpGet);
            Document document = Jsoup.parse(httpResponse.getEntity().getContent(), "UTF-8", SamSensor
                    .SAM_DATA_BASE_URL);
            System.out.println(document);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            closeableHttpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CloseableHttpClient getDefaultHttpClient() {
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new
                PoolingHttpClientConnectionManager();
        poolingHttpClientConnectionManager.setMaxTotal(10);

        CloseableHttpClient closeableHttpClient = HttpClients.custom().setConnectionManager
                (poolingHttpClientConnectionManager).build();
        return closeableHttpClient;
    }

    public static CloseableHttpClient getDefaultHttpClient(int maxConnections) {
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new
                PoolingHttpClientConnectionManager();
        poolingHttpClientConnectionManager.setMaxTotal(maxConnections);
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(1);

        CloseableHttpClient closeableHttpClient = HttpClients.custom().setConnectionManager
                (poolingHttpClientConnectionManager).build();
        return closeableHttpClient;
    }

    public static RequestConfig getCustomtRequestConfig() {
        return RequestConfig.custom().setConnectTimeout
                (10000).setSocketTimeout(10000).setMaxRedirects(3).build();
    }

    public static RequestConfig getDefaultRequestConfig() {
        return RequestConfig.custom().setMaxRedirects(3).setConnectTimeout((int) TimeUnit.SECONDS.toMillis(30l))
                .setSocketTimeout((int) TimeUnit.MINUTES.toMillis(1l)).build();
    }
}
