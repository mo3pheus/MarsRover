package space.exploration.mars.rover.sensor.sam;

import junit.framework.TestCase;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import space.exploration.mars.rover.sensor.SamSensor;

import java.io.IOException;

public class UrlExtractTaskTest extends TestCase {
    @Test
    public void testUrlExtractTask() {
        String url = SamSensor.SAM_DATA_BASE_URL + "eid25208/level2/";
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
            assertTrue(document != null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            httpGet.releaseConnection();
            closeableHttpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
