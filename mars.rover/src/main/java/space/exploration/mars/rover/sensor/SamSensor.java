package space.exploration.mars.rover.sensor;

import com.google.protobuf.ByteString;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.service.SampleAnalysisDataOuterClass;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.mars.rover.service.calibration.spectrometer.sam.*;
import space.exploration.mars.rover.service.calibration.spectrometer.sam.SamCalibrationUtil.CALIBRATION_STATUS_CODE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;

public class SamSensor implements IsEquipment {
    public static final String SAM_DATA_BASE_URL = "http://pds-geosciences.wustl" +
            ".edu/msl/msl-m-sam-2-rdr-l0-v1/mslsam_1xxx/data/";
    public static final String LIFESPAN          = "mars.rover.sam.lifespan";

    private Map<Integer, DataAvailabilityPacket> dataAvailabilityPacketMap = new HashMap<>();
    private CALIBRATION_STATUS_CODE              calibrationStatusCode     = null;
    private Logger                               logger                    = LoggerFactory.getLogger
            (SamSensor.class);
    private CloseableHttpClient                  httpClient                = null;
    private RequestConfig                        requestConfig             = null;
    private Properties                           calibrationProperties     = null;
    private int                                  lifespan                  = 0;
    private int                                  useCount                  = 0;
    private DataAvailabilityPacket               currentDataPacket         = null;

    List<Future<DataAvailabilityPacket>> resultList      = new ArrayList<>();
    List<IsCalibrationService>           urlExtractTasks = new ArrayList<>();
    ExecutorService                      executorService = null;

    public SamSensor(Properties samCalibrationProperties) {
        this.calibrationProperties = samCalibrationProperties;
        this.lifespan = Integer.parseInt(calibrationProperties.getProperty(LIFESPAN));
        calibrationStatusCode = CALIBRATION_STATUS_CODE.INIT;
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

    public int getUseCount() {
        return useCount;
    }

    public boolean isCalibrated() {
        return (calibrationStatusCode == CALIBRATION_STATUS_CODE.SUCCESS);
    }

    public ByteString getSamData() {
        List<File> dataFiles = new ArrayList<>();
        if (calibrationStatusCode == CALIBRATION_STATUS_CODE.IN_PROGRESS
                || calibrationStatusCode == CALIBRATION_STATUS_CODE.INIT
        ) {
            logger.info("Can not get samData at the moment. SAM Sensor is in calibration mode. Please try again later" +
                                ".");
        } else if (calibrationStatusCode == CALIBRATION_STATUS_CODE.ERROR) {
            logger.error("Sam Calibration failed. Sam sensor unavailable");
        } else {
            lifespan--;
            useCount++;

            logger.info("Current data packet contains => " + currentDataPacket.toString());
            try {
                dataFiles = currentDataPacket.downloadFiles();
                logger.info("Files downloaded = " + dataFiles.size());

            } catch (IOException e) {
                logger.info("IOException while downloading files = ", e);
            }
        }

        SampleAnalysisDataOuterClass.SampleAnalysisData.Builder sBuilder =
                SampleAnalysisDataOuterClass.SampleAnalysisData
                        .newBuilder();
        try {
            for (File dataFile : dataFiles) {
                byte[] content = Files.readAllBytes(dataFile.toPath());

                SampleAnalysisDataOuterClass.SampleAnalysisData.SampleDataFile.Builder sDataBuilder =
                        SampleAnalysisDataOuterClass.SampleAnalysisData.SampleDataFile
                                .newBuilder();
                sDataBuilder.setContent(ByteString.copyFrom(content));
                sDataBuilder.setFileName(dataFile.getName());

                sBuilder.addDataFiles(sDataBuilder.build());
            }
            sBuilder.setSol(currentDataPacket.getSol());
        } catch (IOException io) {
            logger.error("Error while reading file content.", io);
        }
        return sBuilder.build().toByteString();
    }

    public void setErrorState() {
        calibrationStatusCode = CALIBRATION_STATUS_CODE.ERROR;
    }

    @Override
    public String toString() {
        return "Total sols available for SAM = " + dataAvailabilityPacketMap;
    }

    private void populateTaskList() {
        logger.debug("Populating taskList to crawl data source");
        SamBaseUrlCrawl samBaseUrlCrawl = new SamBaseUrlCrawl();
        httpClient = SamCalibrationUtil.getDefaultHttpClient(samBaseUrlCrawl.getExperimentIds().size());
        for (String experiment : samBaseUrlCrawl.getExperimentIds()) {
            urlExtractTasks.add(new UrlExtractTask(httpClient, experiment, requestConfig));
        }
    }

    private void init() {
        logger.debug("Initializing executorService and requestConfig");
        requestConfig = SamCalibrationUtil.getDefaultRequestConfig();
        executorService = Executors.newFixedThreadPool(urlExtractTasks.size());
    }

    private void fireTasks() {
        logger.debug("Firing tasks");
        try {
            calibrationStatusCode = CALIBRATION_STATUS_CODE.IN_PROGRESS;
            resultList = executorService.invokeAll(urlExtractTasks);
        } catch (Exception e) {
            logger.info("SamSensor calibration encountered error" + e.getMessage());
            calibrationStatusCode = CALIBRATION_STATUS_CODE.ERROR;
        }
    }

    private void cleanUp() {
        logger.debug("Calibration cleanup!");
        try {
            httpClient.close();
            executorService.shutdown();
        } catch (IOException e) {
            logger.error("Error while closing client.", e);
        }
    }

    private void populateAvailabilityMap() {
        logger.debug("Populating data availability map");
        for (int i = 0; i < urlExtractTasks.size(); i++) {
            Future<DataAvailabilityPacket> responseFuture = resultList.get(i);
            try {
                DataAvailabilityPacket dataAvailabilityPacket = responseFuture.get();
                dataAvailabilityPacketMap.put(dataAvailabilityPacket.getSol(), dataAvailabilityPacket);
            } catch (Exception e) {
                logger.error("Error while executing samCalibration for experiment");
                continue;
            }
        }
    }

    @Override
    public int getLifeSpan() {
        return lifespan;
    }

    @Override
    public String getEquipmentName() {
        return "SAM Sensor";
    }

    @Override
    public boolean isEndOfLife() {
        return (lifespan <= 0);
    }

    @Override
    public long getRequestMetric() {
        return useCount;
    }

    @Override
    public String getEquipmentLifeSpanProperty() {
        return LIFESPAN;
    }

    public void updateDataCache(int sol) {
        logger.info("Updating dataCache for samSensor - sol updated to " + sol);

        if (!dataAvailabilityPacketMap.containsKey(sol)) {
            logger.error("Sam data unavailable for sol = " + sol);
        } else {
            currentDataPacket = dataAvailabilityPacketMap.get(sol);
            logger.info("Data links found sol = " + sol + " data links :: " + currentDataPacket.toString());
        }
    }

    public Map<Integer, DataAvailabilityPacket> getDataAvailabilityPacketMap() {
        return dataAvailabilityPacketMap;
    }
}
