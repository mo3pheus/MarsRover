package space.exploration.mars.rover.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.service.DanRDRData;
import space.exploration.communications.protocol.service.DanRDRDataSeriesOuterClass;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.mars.rover.kernel.Rover;
import space.exploration.mars.rover.service.DANCalibrationService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DANSpectrometer implements IsEquipment {
    public static final String                          LIFESPAN       = "mars.rover.dan.spectrometer.lifespan";
    public static final String                          DAN_UTC_FORMAT = "yyyy-MM-dd~HH:mm:ss.sss";
    private             Logger                          logger         = LoggerFactory.getLogger(DANSpectrometer.class);
    private             int                             lifeSpan       = 0;
    private             int                             sol            = 0;
    private             Rover                           rover          = null;
    private             List<DanRDRData.DANDerivedData> danDerivedData = null;

    public DANSpectrometer(Rover rover) {
        this.rover = rover;
        danDerivedData = new ArrayList<>();
        try {
            lifeSpan = Integer.parseInt(rover.getRoverConfig().getMarsConfig().getProperty(LIFESPAN));
        } catch (NumberFormatException nfe) {
            logger.error("Property not found in marsConfig - mars.rover.dan.lifespan . Defaulting the value to 1000 " +
                                 "", nfe);
            lifeSpan = 1000;
        }
    }

    public DanRDRDataSeriesOuterClass.DanRDRDataSeries scanForWater() {
        DanRDRDataSeriesOuterClass.DanRDRDataSeries.Builder dBuilder = DanRDRDataSeriesOuterClass.DanRDRDataSeries
                .newBuilder();
        if (danDerivedData != null) {
            lifeSpan--;
            dBuilder.addAllDanData(danDerivedData);
        }
        dBuilder.setSol(sol);
        return dBuilder.build();
    }

    @Override
    public int getLifeSpan() {
        return lifeSpan;
    }

    @Override
    public String getEquipmentName() {
        return "DANSpectrometer";
    }

    @Override
    public boolean isEndOfLife() {
        return (lifeSpan > 0);
    }

    @Override
    public long getRequestMetric() {
        return rover.getDanSensingState().getRequests().count();
    }

    @Override
    public String getEquipmentLifeSpanProperty() {
        return LIFESPAN;
    }

    public void calibrateDanSensor(int sol) {
        logger.info("Commencing calibration for DAN Spectrometer for sol = " + sol);
        this.sol = sol;
        DANCalibrationService danCalibrationService = new DANCalibrationService(sol);
        try {
            this.danDerivedData = danCalibrationService.getDanPayload();
        } catch (IOException e) {
            logger.info("Encountered exception when calibrating DAN Spectrometer, DANDerivedRDRData may not be " +
                                "available for sol = " + sol, e);
        }
    }
}
