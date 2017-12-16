package space.exploration.mars.rover.sensor;

import communications.protocol.ModuleDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.spectrometer.SpectrometerScanOuterClass;
import space.exploration.mars.rover.environment.EnvironmentUtils;
import space.exploration.mars.rover.environment.SoilComposition;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.mars.rover.kernel.Rover;
import space.exploration.mars.rover.utils.RoverUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sanketkorgaonkar on 5/2/17.
 */
public class Spectrometer implements IsEquipment {
    public static final  String                      LIFESPAN         = "mars.rover.spectrometer.lifeSpan";
    private static final int                         NUM_LAST_SCANS   = 10;
    private              Logger                      logger           = LoggerFactory.getLogger(Spectrometer.class);
    private              Point                       origin           = null;
    private              Map<Point, SoilComposition> surfaceComp      = null;
    private              List<SoilComp>              scanAreaComp     = null;
    private              Rover                       rover            = null;
    private              int                         cellWidth        = 0;
    private              int                         frameWidth       = 0;
    private              int                         lifeSpan         = 0;
    private              int                         powerConsumption = 0;
    private              boolean                     endOfLife        = false;

    public Spectrometer(Point origin, Rover rover) {
        this.origin = origin;
        this.rover = rover;
        this.frameWidth = Integer.parseInt(rover.getMarsConfig().getProperty(EnvironmentUtils.FRAME_WIDTH_PROPERTY));
        this.powerConsumption = Integer.parseInt(rover.getMarsConfig().getProperty(EnvironmentUtils
                                                                                           .SPECTROMETER_POWER_CONSUMPTION));
        scanAreaComp = new ArrayList<SoilComp>();
        RoverUtil.roverSystemLog(logger, "Spectrometer initialized and ready!", "INFO");
    }

    public void setSurfaceComp(Map<Point, SoilComposition> surfaceComp) {
        this.surfaceComp = surfaceComp;
    }

    public void setCellWidth(int cellWidth) {
        this.cellWidth = cellWidth;
    }

    public List<SoilComp> getScanAreaComp() {
        return this.scanAreaComp;
    }

    public int getLifeSpan() {
        return lifeSpan;
    }

    public void setLifeSpan(int lifeSpan) {
        this.lifeSpan = lifeSpan;
    }

    public boolean isEndOfLife() {
        return endOfLife;
    }

    public void setEndOfLife(boolean endOfLife) {
        this.endOfLife = endOfLife;
    }

    public String getEquipmentName() {
        return "Spectrometer";
    }

    public void processSurroundingArea() {
        if (lifeSpan <= 0) {
            return;
        }

        if (lifeSpan < NUM_LAST_SCANS && !endOfLife) {
            endOfLife = true;
            rover.setEquipmentEOL(endOfLife);
            rover.authorizeTransmission(ModuleDirectory.Module.SENSOR_SPECTROMETER, RoverUtil.getEndOfLifeMessage
                    (ModuleDirectory.Module.SENSOR_SPECTROMETER, " Spectrometer at end of life. Num last scans left " +
                            "=" + NUM_LAST_SCANS + " Please confirm command. The" +
                            " next command will be honored!", rover).toByteArray
                    ());
            return;
        } else if (surfaceComp == null || cellWidth == 0) {
            logger.error("SurfaceComp or cellWidth not set for spectrometer");
            rover.writeErrorLog("SurfaceComp or cellWidth not set for spectrometer!", null);
            return;
        }

        lifeSpan--;
        for (int i = ((int) origin.getX() - cellWidth); i <= ((int) origin.getX() + cellWidth); i = (i + cellWidth)) {
            for (int j = ((int) origin.getY() - cellWidth); j <= ((int) origin.getY() + cellWidth); j = (j
                    + cellWidth)) {
                if (i < 0 || j < 0 || i >= frameWidth || j > frameWidth) {
                    // If the point is outside the defined grid,
                    // soil composition will be invalid
                    continue;
                }

                Point           temp        = new Point(i, j);
                SoilComposition composition = surfaceComp.get(temp);
                SoilComp        comp        = new SoilComp(temp, composition);
                scanAreaComp.add(comp);
                logger.debug(" Processing point = " + temp.toString() + " Composition = " + comp.toString());
            }
        }
    }

    public List<Point> getExplorationPoints() {
        List<Point> scanPoints = new ArrayList<Point>();
        for (SoilComp sc : scanAreaComp) {
            scanPoints.add(sc.getPoint());
        }
        return scanPoints;
    }

    public SpectrometerScanOuterClass.SpectrometerScan getSpectrometerReading() {
        SpectrometerScanOuterClass.SpectrometerScan.Builder sBuilder = SpectrometerScanOuterClass
                .SpectrometerScan.newBuilder();
        List<SpectrometerScanOuterClass.SpectrometerScan.PointComp> sampleReadings = new
                ArrayList<SpectrometerScanOuterClass.SpectrometerScan.PointComp>();

        for (SoilComp sample : scanAreaComp) {
            SpectrometerScanOuterClass.SpectrometerScan.PointComp.Builder pBuilder = SpectrometerScanOuterClass
                    .SpectrometerScan.PointComp.newBuilder();

            Point temp = sample.getPoint();
            pBuilder.setPoint(SpectrometerScanOuterClass.SpectrometerScan.Location.newBuilder().setX(temp.x).setY
                    (temp.y).build());

            SoilComposition composition = sample.getComposition();
            pBuilder.setSoilComp(SpectrometerScanOuterClass.SpectrometerScan.Composition.newBuilder().setAl2O3
                    (composition.getAl2O3()).setFeO(composition.getFeO())
                                         .setMgO(composition.getMgO()).setNa2O(composition.getNa2O()).build());
            sampleReadings.add(pBuilder.build());
        }
        sBuilder.addAllScanAreaComp(sampleReadings);
        return sBuilder.build();
    }

    public class SoilComp {
        private Point           point;
        private SoilComposition composition;

        public SoilComp(Point point, SoilComposition soilComposition) {
            this.point = point;
            this.composition = soilComposition;
        }

        public Point getPoint() {
            return point;
        }

        public void setPoint(Point point) {
            this.point = point;
        }

        public SoilComposition getComposition() {
            return composition;
        }

        public void setComposition(SoilComposition composition) {
            this.composition = composition;
        }

        public String toString() {
            return " Point = " + point.toString() + " Composition = " + composition.toString();
        }
    }
}
