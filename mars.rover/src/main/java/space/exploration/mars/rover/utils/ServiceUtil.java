package space.exploration.mars.rover.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.sensors.apxs.ApxsData;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class ServiceUtil {
    public static final Logger logger = LoggerFactory.getLogger(ServiceUtil.class);

    public static final byte[] download(String urlString) throws IOException {
        URL           url = new URL(urlString);
        URLConnection uc  = url.openConnection();
        int           len = uc.getContentLength();
        InputStream   is  = new BufferedInputStream(uc.getInputStream());
        try {
            byte[] data   = new byte[len];
            int    offset = 0;
            while (offset < len) {
                int read = is.read(data, offset, data.length - offset);
                if (read < 0) {
                    break;
                }
                offset += read;
            }
            if (offset < len) {
                throw new IOException(
                        String.format("Read %d bytes; expected %d", offset, len));
            }
            return data;
        } finally {
            is.close();
        }
    }

    public static final File downloadCsv(String urlString, String fileName) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        fileOutputStream.write(download(urlString));
        return new File(fileName);
    }

    public static final ApxsData.ApxsDataPacket extractApxsData(File apxsDataFile) throws IOException {
        ApxsData.ApxsDataPacket.Builder apxsBuilder = ApxsData.ApxsDataPacket.newBuilder();

        BufferedReader bufferedReader = new BufferedReader(new FileReader(apxsDataFile));
        String         dataLine       = null;
        boolean        first          = true;
        while ((dataLine = bufferedReader.readLine()) != null) {
            if (!first) {
                String[] dataParts = dataLine.split(",");

                ApxsData.ApxsDataPacket.apxsElement.Builder elementBuilder = ApxsData.ApxsDataPacket.apxsElement
                        .newBuilder();
                elementBuilder.setOxide(dataParts[0]);
                elementBuilder.setPercentage(Double.parseDouble(dataParts[1]));
                elementBuilder.setUncertainty(Double.parseDouble(dataParts[2]));
                apxsBuilder.addElementComposition(elementBuilder.build());
            } else {
                first = false;
            }
        }

        return apxsBuilder.build();
    }
}
