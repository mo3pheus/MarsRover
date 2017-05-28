package space.exploration.mars.rover.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.forkjoin.ThreadLocalRandom;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.mars.rover.kernel.ModuleDirectory;
import space.exploration.mars.rover.kernel.Rover;
import space.exploration.mars.rover.utils.CameraUtil;
import space.exploration.mars.rover.utils.RoverUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by sanketkorgaonkar on 5/9/17.
 */
public class Camera implements IsEquipment {
    private static final String          LIFESPAN           = "mars.rover.camera.lifeSpan";
    private static final int             LAST_SHOTS_RESERVE = 10;
    private              int             numImages          = 0;
    private              int             numImageCaches     = 0;
    private              int             lifeSpan           = 0;
    private              long            shutterSpeed       = 0;
    private              Properties      marsConfig         = null;
    private              BufferedImage[] marsImages         = null;
    private              List<Point>     imageCachePoints   = null;
    private              Logger          logger             = LoggerFactory.getLogger(Camera.class);
    private              Rover           rover              = null;
    private              boolean         endOfLife          = false;

    public Camera(Properties marsConfig, Rover rover) {
        this.marsConfig = marsConfig;
        this.rover = rover;
        numImages = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.CAMERA_NUM_IMAGES));
        numImageCaches = Integer.parseInt(marsConfig.getProperty(EnvironmentUtils.CAMERA_NUM_IMAGE_CAHCES));
        shutterSpeed = Long.parseLong(marsConfig.getProperty(EnvironmentUtils.CAMERA_SHUTTER_SPEED));
        lifeSpan = Integer.parseInt(marsConfig.getProperty(LIFESPAN));
        marsImages = new BufferedImage[numImages];
        imageCachePoints = new ArrayList<Point>();
        collectImages();
        loadImageCachePoints();

        RoverUtil.roverSystemLog(logger, "Camera initialized and ready!", "INFO");
    }

    public byte[] takePhoto(Point location) {
        if (lifeSpan <= LAST_SHOTS_RESERVE && !endOfLife) {
            endOfLife = true;
            rover.setEquipmentEOL(endOfLife);
            rover.authorizeTransmission(ModuleDirectory.Module.CAMERA_SENSOR, RoverUtil.getEndOfLifeMessage
                    (ModuleDirectory.Module.CAMERA_SENSOR, "Camera at end of life. Last " + Integer.toString
                            (LAST_SHOTS_RESERVE) +
                                                           " remaining. Please confirm the location for final shots. " +
                                                           "The next command will be honored!", rover).toByteArray());
            return null;
        }

        lifeSpan--;
        if (imageCachePoints.contains(location)) {
            int    index      = ThreadLocalRandom.current().nextInt(0, numImages);
            byte[] imageBytes = null;
            try {
                imageBytes = CameraUtil.convertImageToByteArray(marsImages[index]);
            } catch (IOException io) {
                logger.error(io.getMessage());
            }
            return imageBytes;
        } else {
            return null;
        }
    }

    public boolean isEndOfLife() {
        return endOfLife;
    }

    public void setEndOfLife(boolean endOfLife) {
        this.endOfLife = endOfLife;
    }

    public long getShutterSpeed() {
        return shutterSpeed;
    }

    public int getLifeSpan() {
        return lifeSpan;
    }

    @Override
    public String getEquipmentName() {
        return "Camera";
    }

    private final void collectImages() {
        for (int i = 0; i < numImages; i++) {
            String fileName = EnvironmentUtils.CAMERA_IMAGE_HEADER + Integer.toString(i + 1) + ".jpg";
            try {
                marsImages[i] = ImageIO.read(new File(Camera.class.getResource(fileName).getPath()));
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
