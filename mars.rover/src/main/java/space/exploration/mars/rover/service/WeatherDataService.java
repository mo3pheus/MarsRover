package space.exploration.mars.rover.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;

import static space.exploration.mars.rover.utils.ServiceUtil.download;

public class WeatherDataService {
    public static final String  REMS_CALIBRATION_FILE  = "remsCalibrationFile.dat";
    private             Logger  logger                 = LoggerFactory.getLogger(WeatherDataService
                                                                                         .class);
    private             boolean calibrated             = false;
    private volatile    File    weatherCalibrationFile = null;

    public File downloadCalibrationData(int sol) {
        String targetURL = RemsDataService.getFileURL(sol);

        if (downloadFile(targetURL)) {
            return weatherCalibrationFile;
        } else {
            return null;
        }
    }

    public boolean isCalibrated() {
        return calibrated;
    }

    public File getWeatherCalibrationFile() {
        return weatherCalibrationFile;
    }

    private boolean downloadFile(String url) {
        try {
            byte[]           weatherContents = download(url);
            FileOutputStream fos             = new FileOutputStream(REMS_CALIBRATION_FILE);
            fos.write(weatherContents);
            weatherCalibrationFile = new File(REMS_CALIBRATION_FILE);
            fos.close();
            calibrated = true;
        } catch (Exception e) {
            logger.debug("Failed for url = " + url);
            calibrated = false;
            weatherCalibrationFile = null;
        }
        return calibrated;
    }
}
