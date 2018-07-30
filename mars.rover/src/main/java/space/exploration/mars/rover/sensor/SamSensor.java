package space.exploration.mars.rover.sensor;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.service.calibration.spectrometer.sam.*;
import space.exploration.mars.rover.service.calibration.spectrometer.sam.SamCalibrationUtil.CALIBRATION_STATUS_CODE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class SamSensor {
    public static final String SAM_DATA_BASE_URL = "http://pds-geosciences.wustl" +
            ".edu/msl/msl-m-sam-2-rdr-l0-v1/mslsam_1xxx/data/";

    private          Map<Integer, DataAvailabilityPacket> dataAvailabilityPacketMap = new HashMap<>();
    private          CALIBRATION_STATUS_CODE              calibrationStatusCode     = null;
    private          Logger                               logger                    = LoggerFactory.getLogger
            (SamSensor.class);
    private volatile CloseableHttpClient                  httpClient                = null;
    private          RequestConfig                        requestConfig             = null;

    List<Future<DataAvailabilityPacket>> resultList      = new ArrayList<>();
    List<IsCalibrationService>           urlExtractTasks = new ArrayList<>();
    ExecutorService                      executorService = null;

    public SamSensor(File samCalibrationFile) {
        calibrationStatusCode = CALIBRATION_STATUS_CODE.INIT;
        if (samCalibrationFile != null) {
            // load from serialized file
        } else {
            populateTaskList();
            init();
            fireTasks();
            populateAvailabilityMap();
            cleanUp();

            calibrationStatusCode = (dataAvailabilityPacketMap.keySet().isEmpty()) ? CALIBRATION_STATUS_CODE.ERROR :
                    CALIBRATION_STATUS_CODE.SUCCESS;
            logger.debug("Sam data availability :: " + dataAvailabilityPacketMap);
            logger.info("Sam Sensor successfully calibrated.");
        }
    }

    public boolean isCalibrated() {
        return (calibrationStatusCode == CALIBRATION_STATUS_CODE.SUCCESS);
    }

    public void setErrorState() {
        calibrationStatusCode = CALIBRATION_STATUS_CODE.ERROR;
    }

    @Override
    public String toString() {
        return "Total sols available for SAM = " + dataAvailabilityPacketMap;
    }

    private void populateTaskList() {
        logger.info("Populating taskList to crawl data source");
        SamBaseUrlCrawl samBaseUrlCrawl = new SamBaseUrlCrawl();
        httpClient = SamCalibrationUtil.getDefaultHttpClient(samBaseUrlCrawl.getExperimentIds().size());
        for (String experiment : samBaseUrlCrawl.getExperimentIds()) {
            urlExtractTasks.add(new UrlExtractTask(httpClient, experiment, requestConfig));
        }
    }

    private void init() {
        logger.info("Initializing executorService and requestConfig");
        requestConfig = SamCalibrationUtil.getDefaultRequestConfig();
        executorService = Executors.newFixedThreadPool(urlExtractTasks.size());
    }

    private void fireTasks() {
        logger.info("Firing tasks");
        try {
            calibrationStatusCode = CALIBRATION_STATUS_CODE.IN_PROGRESS;
            resultList = executorService.invokeAll(urlExtractTasks);
        } catch (Exception e) {
            logger.info("SamSensor calibration encountered error" + e.getMessage());
            calibrationStatusCode = CALIBRATION_STATUS_CODE.ERROR;
        }
    }

    private void cleanUp() {
        logger.info("Calibration cleanup!");
        try {
            httpClient.close();
            executorService.shutdown();
        } catch (IOException e) {
            logger.error("Error while closing client.", e);
        }
    }

    private void populateAvailabilityMap() {
        logger.info("Populating data availability map");
        for (int i = 0; i < urlExtractTasks.size(); i++) {
            Future<DataAvailabilityPacket> responseFuture = resultList.get(i);
            try {
                DataAvailabilityPacket dataAvailabilityPacket = responseFuture.get();
                dataAvailabilityPacketMap.put(dataAvailabilityPacket.getSol(), dataAvailabilityPacket);
            } catch (ExecutionException e) {
                logger.error("Error while executing samCalibration for experiment");
                continue;
            } catch (InterruptedException ie) {
                logger.error("Error while executing samCalibration for experiment");
            }
        }
    }
}
