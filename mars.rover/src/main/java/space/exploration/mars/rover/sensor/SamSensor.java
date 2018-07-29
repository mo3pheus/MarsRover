package space.exploration.mars.rover.sensor;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.service.calibration.spectrometer.sam.DataAvailabilityPacket;
import space.exploration.mars.rover.service.calibration.spectrometer.sam.SamBaseUrlCrawl;
import space.exploration.mars.rover.service.calibration.spectrometer.sam.SamCalibrationUtil;
import space.exploration.mars.rover.service.calibration.spectrometer.sam.SamCalibrationUtil.CALIBRATION_STATUS_CODE;
import space.exploration.mars.rover.service.calibration.spectrometer.sam.UrlExtractTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SamSensor {
    public static final String SAM_DATA_BASE_URL = "http://pds-geosciences.wustl" +
            ".edu/msl/msl-m-sam-2-rdr-l0-v1/mslsam_1xxx/data/";

    private          Map<Integer, DataAvailabilityPacket> dataAvailabilityPacketMap = new HashMap<>();
    private          CALIBRATION_STATUS_CODE              calibrationStatusCode     = null;
    private          Logger                               logger                    = LoggerFactory.getLogger
            (SamSensor.class);
    private volatile CloseableHttpClient                  httpClient                = null;
    private          RequestConfig                        requestConfig             = null;

    public SamSensor(File samCalibrationFile) {
        calibrationStatusCode = CALIBRATION_STATUS_CODE.INIT;
        if (samCalibrationFile != null) {
            // load from serialized file
        } else {
            requestConfig = SamCalibrationUtil.getDefaultRequestConfig();

            SamBaseUrlCrawl samBaseUrlCrawl = new SamBaseUrlCrawl();
            httpClient = SamCalibrationUtil.getDefaultHttpClient(samBaseUrlCrawl.getExperimentIds().size());
            List<UrlExtractTask> urlExtractTasks = new ArrayList<>();
            for (String experiment : samBaseUrlCrawl.getExperimentIds()) {
                urlExtractTasks.add(new UrlExtractTask(httpClient, experiment, requestConfig));
            }

            ExecutorService executorService = Executors.newFixedThreadPool(urlExtractTasks.size());
            try {
                calibrationStatusCode = CALIBRATION_STATUS_CODE.IN_PROGRESS;
                executorService.invokeAll(urlExtractTasks);
                executorService.shutdown();
            } catch (Exception e) {
                logger.debug("SamSensor calibration encountered error", e);
                calibrationStatusCode = CALIBRATION_STATUS_CODE.ERROR;
            }

            try {
                for (UrlExtractTask urlExtractTask : urlExtractTasks) {
                    DataAvailabilityPacket dataAvailabilityPacket = urlExtractTask.getDataAvailability();
                    if (dataAvailabilityPacket != null) {
                        dataAvailabilityPacketMap.put(dataAvailabilityPacket.getSol(), dataAvailabilityPacket);
                    }
                }
                httpClient.close();
            } catch (Exception e) {
                logger.error("SamSensor calibration encountered error", e);
            }

            calibrationStatusCode = (dataAvailabilityPacketMap.keySet().isEmpty()) ? CALIBRATION_STATUS_CODE.ERROR :
                    CALIBRATION_STATUS_CODE.SUCCESS;
            logger.info("Sam Sensor successfully calibrated.");
            logger.info("Sam data availability :: " + dataAvailabilityPacketMap);
        }
    }

    public boolean isCalibrated() {
        return (calibrationStatusCode == CALIBRATION_STATUS_CODE.SUCCESS);
    }

    @Override
    public String toString() {
        return "Total sols available for SAM = " + dataAvailabilityPacketMap;
    }
}
