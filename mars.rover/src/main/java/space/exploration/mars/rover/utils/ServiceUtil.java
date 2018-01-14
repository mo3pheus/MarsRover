package space.exploration.mars.rover.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class ServiceUtil {

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
}
