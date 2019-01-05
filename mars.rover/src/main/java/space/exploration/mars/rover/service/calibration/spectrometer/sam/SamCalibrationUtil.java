package space.exploration.mars.rover.service.calibration.spectrometer.sam;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.util.concurrent.TimeUnit;

public class SamCalibrationUtil {
    public enum CALIBRATION_STATUS_CODE {
        INIT,
        IN_PROGRESS,
        ERROR,
        SUCCESS
    }

    public static CloseableHttpClient getDefaultHttpClient(int maxConnections) {
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new
                PoolingHttpClientConnectionManager();
        poolingHttpClientConnectionManager.setMaxTotal(maxConnections);
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(maxConnections);

        CloseableHttpClient closeableHttpClient = HttpClients.custom().setConnectionManager
                (poolingHttpClientConnectionManager).build();
        return closeableHttpClient;
    }

    public static RequestConfig getDefaultRequestConfig() {
        return RequestConfig.custom().setMaxRedirects(3).setConnectTimeout((int) TimeUnit.SECONDS.toMillis(30l))
                .setSocketTimeout((int) TimeUnit.MINUTES.toMillis(1l)).build();
    }
}
