package space.exploration.mars.rover.sensor;

import scala.concurrent.forkjoin.ThreadLocalRandom;
import space.exploration.mars.rover.environment.EnvironmentUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by sanketkorgaonkar on 5/9/17.
 */
public class Camera {
    private int             numImages        = 0;
    private int             numImageCaches   = 0;
    private long            shutterSpeed     = 0;
    private Properties      marsConfig       = null;
    private BufferedImage[] marsImages       = null;
    private List<Point>     imageCachePoints = null;

    public class Photo implements Serializable {
        BufferedImage image;

        public Photo(BufferedImage image) {
            this.image = image;
        }

        public BufferedImage getImaage() {
            return image;
        }
    }

    public Camera(Properties marsConfig) {
        this.marsConfig = marsConfig;
        numImages = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.CAMERA_NUM_IMAGES));
        numImageCaches = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.CAMERA_NUM_IMAGE_CAHCES));
        shutterSpeed = Long.parseLong(marsConfig.getProperty(EnvironmentUtils.CAMERA_SHUTTER_SPEED));
        marsImages = new BufferedImage[numImageCaches];
        imageCachePoints = new ArrayList<Point>();
        collectImages();
    }

    public Photo takePhoto(Point location) {
        if (imageCachePoints.contains(location)) {
            int index = ThreadLocalRandom.current().nextInt(0, numImages);
            return new Photo(marsImages[index]);
        } else {
            return null;
        }
    }

    public long getShutterSpeed() {
        return shutterSpeed;
    }

    private final void collectImages() {
        for (int i = 0; i < numImages; i++) {
            String fileName = EnvironmentUtils.CAMERA_IMAGE_HEADER + Integer.toString(i) + ".jpg";
            try {
                marsImages[i] = ImageIO.read(new File(Camera.class.getClassLoader().getResource(fileName).getPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private final void loadImageCachePoints() {
        for (int i = 0; i < numImageCaches; i++) {
            String[] pointString = marsConfig.getProperty(EnvironmentUtils.CAMERA_LOCATION_HEADER + Integer.toString
                    (i)).split(",");
            Point point = new Point(Integer.parseInt(pointString[0]), Integer.parseInt(pointString[1]));
            imageCachePoints.add(point);
        }
    }
}
