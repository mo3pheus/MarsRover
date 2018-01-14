package space.exploration.mars.rover.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.spice.utilities.EphemerisConversionUtil;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import static space.exploration.mars.rover.utils.ServiceUtil.download;

public class WeatherDataService {
    public static final  String                  REMS_CALIBRATION_FILE   = "remsCalibrationFile.dat";
    public static final  Integer                 SOL_SECONDS             = 88775;
    private              Logger                  logger                  = LoggerFactory.getLogger(WeatherDataService
                                                                                                           .class);
    private static final String                  URL_BEGIN               = "http://pds-atmospheres.nmsu" +
            ".edu/PDS/data/mslrem_1001/DATA/";
    private              List<WeatherDirectory>  weatherDirectories      = new ArrayList<>();
    private              boolean                 calibrated              = false;
    private              EphemerisConversionUtil ephemerisConversionUtil = new EphemerisConversionUtil();
    private volatile     File                    weatherCalibrationFile  = null;

    public WeatherDataService() {
        initializeWeatherDirectories();
    }

    private String urlString = "";

    public class WeatherDirectory {
        int    startSol;
        int    endSol;
        long   ephemerisTimestamp;
        String directoryString;

        String getDirectoryString(int sol) {
            if (sol >= startSol && sol <= endSol) {
                return directoryString;
            } else {
                return null;
            }
        }

        public long getEphemerisTimestamp() {
            return ephemerisTimestamp;
        }

        public void setEphemerisTimestamp(long ephemerisTimestamp) {
            this.ephemerisTimestamp = ephemerisTimestamp;
        }

        public int getStartSol() {
            return startSol;
        }

        public int getEndSol() {
            return endSol;
        }

        public WeatherDirectory(int startSol, int endSol, long ephemerisTimeStamp, String directoryString) {
            ephemerisConversionUtil.updateClock(Long.toString(ephemerisTimeStamp));
            this.startSol = (ephemerisConversionUtil.getSol() < startSol) ? startSol : ephemerisConversionUtil.getSol();
            this.ephemerisTimestamp = ephemerisTimeStamp;
            this.endSol = endSol;
            this.directoryString = directoryString;
        }
    }

    /**
     * @param sol This function sets the urlString and downloads the calibrationFile.
     *            http://pds-atmospheres.nmsu.edu/PDS/data/mslrem_1001/DATA/ - 0 begin
     *            SOL_00090_00179/ - 1
     *            SOL00092/RME_ - 2
     *            405613716 - 3 etime
     *            RNV - 4
     *            0092 - 5 paddedDir
     *            0000000_______ - 6
     *            P8.TAB - 7 partition
     */
    @Deprecated
    public void downloadCalibrationData(int sol) {
        int          urlCount                   = 0;
        int          startSol                   = getStartSol(sol);
        int          solsDiff                   = sol - startSol;
        double       ephemerisTime              = ((double) getEphemerisStart(sol)) + (solsDiff * SOL_SECONDS);
        List<String> ephemerisTimes             = getEphemerisTimeStrings(ephemerisTime);
        List<String> partitions                 = getPartitionStrings();
        double       totalUrls                  = ephemerisTimes.size() * partitions.size();
        double       progressReportingThreshold = 1.0d;

        for (String url : getURLCombinations(sol)) {
            if (!downloadFile(url)) {
                urlCount++;
                double progress = ((double) urlCount) / totalUrls * 100.0d;
                if (progress >= progressReportingThreshold) {
                    logger.info("REMS Sensor Calibrating Progress = " + progress);
                    progressReportingThreshold++;
                }
                logger.debug("Failed to download for sol = " + sol + " url = " + urlString);
                continue;
            } else {
                return;
            }
        }
    }

    public File downloadCalibrationData(List<String> urls) {
        for (String url : urls) {
            if (!downloadFile(url)) {
                continue;
            } else {
                return weatherCalibrationFile;
            }
        }
        return null;
    }

    public List<String> getURLCombinations(int sol) {
        List<String> urls           = new ArrayList<>();
        int          startSol       = getStartSol(sol);
        int          solsDiff       = sol - startSol;
        String       piece1         = findDirectory(sol);
        String       piece2         = "SOL" + getPaddedDirectoryString(sol) + "/RME_";
        String       solPart        = generateSolPart(sol);
        double       ephemerisTime  = ((double) getEphemerisStart(sol)) + (solsDiff * SOL_SECONDS);
        List<String> ephemerisTimes = getEphemerisTimeStrings(ephemerisTime);
        List<String> partitions     = getPartitionStrings();

        for (String eTime : ephemerisTimes) {
            for (String partition : partitions) {
                String urlCombination = URL_BEGIN + piece1 + piece2 + eTime + "RNV" + solPart +
                        "0000000_______" + partition;
                urls.add(urlCombination);
            }
        }
        return urls;
    }

    public boolean isCalibrated() {
        return calibrated;
    }

    public File getWeatherCalibrationFile() {
        return weatherCalibrationFile;
    }

    private String generateSolPart(int sol) {
        String solPart         = "";
        int    solStringLength = Integer.toString(sol).length();
        for (int i = 0; i < (4 - solStringLength); i++) {
            solPart += "0";
        }
        solPart += Integer.toString(sol);
        return solPart;
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

    private final void initializeWeatherDirectories() {
        WeatherDirectory weatherDirectory = new WeatherDirectory(1, 89, 397535244, "SOL_00001_00089/");
        weatherDirectories.add(weatherDirectory);
        weatherDirectory = new WeatherDirectory(90, 179, 405436167, "SOL_00090_00179/");
        weatherDirectories.add(weatherDirectory);
        weatherDirectory = new WeatherDirectory(180, 269, 413425865, "SOL_00180_00269/");
        weatherDirectories.add(weatherDirectory);
        weatherDirectory = new WeatherDirectory(270, 359, 421415563, "SOL_00270_00359/");
        weatherDirectories.add(weatherDirectory);
        weatherDirectory = new WeatherDirectory(360, 449, 429849133, "SOL_00360_00449/");
        weatherDirectories.add(weatherDirectory);
        weatherDirectory = new WeatherDirectory(450, 583, 437750054, "SOL_00450_00583/");
        weatherDirectories.add(weatherDirectory);
        weatherDirectory = new WeatherDirectory(584, 707, 449290727, "SOL_00584_00707/");
        weatherDirectories.add(weatherDirectory);
        weatherDirectory = new WeatherDirectory(708, 804, 460298753, "SOL_00708_00804/");
        weatherDirectories.add(weatherDirectory);
        weatherDirectory = new WeatherDirectory(805, 938, 468909871, "SOL_00805_00938/");
        weatherDirectories.add(weatherDirectory);
        weatherDirectory = new WeatherDirectory(939, 1062, 480805641, "SOL_00939_01062/");
        weatherDirectories.add(weatherDirectory);
        weatherDirectory = new WeatherDirectory(1063, 1159, 491813667, "SOL_01063_01159/");
        weatherDirectories.add(weatherDirectory);
        weatherDirectory = new WeatherDirectory(1160, 1293, 500424785, "SOL_01160_01293/");
        weatherDirectories.add(weatherDirectory);
        weatherDirectory = new WeatherDirectory(1294, 1417, 512320555, "SOL_01294_01417/");
        weatherDirectories.add(weatherDirectory);
        weatherDirectory = new WeatherDirectory(1418, 1514, 523328582, "SOL_01418_01514/");
        weatherDirectories.add(weatherDirectory);
        weatherDirectory = new WeatherDirectory(1515, 1648, 531939699, "SOL_01515_01648/");
        weatherDirectories.add(weatherDirectory);
        weatherDirectory = new WeatherDirectory(1649, 1772, 543835469, "SOL_01649_01772/");
        weatherDirectories.add(weatherDirectory);
    }

    private String findDirectory(int sol) {
        String directory = null;
        for (WeatherDirectory weatherDirectory : weatherDirectories) {
            if (weatherDirectory.getDirectoryString(sol) != null) {
                directory = weatherDirectory.getDirectoryString(sol);
                break;
            }
        }
        return directory;
    }

    private String getPaddedDirectoryString(int sol) {
        int    numOfDigits = Integer.toString(sol).length();
        int    numZeros    = 5 - numOfDigits;
        String directory   = "";
        for (int i = 0; i < numZeros; i++) {
            directory += "0";
        }
        directory = directory + Integer.toString(sol);
        return directory;
    }

    private final List<String> getEphemerisTimeStrings(double ephemerisTime) {
        List<String> ephTimes = new ArrayList<>();
        for (int i = -150; i < 150; i++) {
            long ephTime = ((long) ephemerisTime);
            ephTime += i;
            ephTimes.add(Long.toString(ephTime));
        }
        return ephTimes;
    }

    private final List<String> getPartitionStrings() {
        List<String> partitions = new ArrayList<>();
        for (int i = 0; i <= 9; i++) {
            partitions.add("P" + Integer.toString(i) + ".TAB");
        }
        return partitions;
    }

    private long getEphemerisStart(int sol) {
        for (WeatherDirectory weatherDirectory : weatherDirectories) {
            if (weatherDirectory.getDirectoryString(sol) != null) {
                return weatherDirectory.getEphemerisTimestamp();
            }
        }
        return -1l;
    }

    private int getStartSol(int sol) {
        for (WeatherDirectory weatherDirectory : weatherDirectories) {
            if (weatherDirectory.getDirectoryString(sol) != null) {
                return weatherDirectory.getStartSol();
            }
        }
        return -1;
    }
}
