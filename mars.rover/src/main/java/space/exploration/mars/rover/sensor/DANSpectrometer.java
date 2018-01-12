package space.exploration.mars.rover.sensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.communications.protocol.service.DanRDRData;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.mars.rover.kernel.Rover;
import space.exploration.mars.rover.service.DANCalibrationService;
import space.exploration.mars.rover.utils.SensorUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DANSpectrometer implements IsEquipment {
    public static final String                          DAN_UTC_FORMAT = "yyyy-MM-ddThh:mm:ss.sss";
    private             Logger                          logger         = LoggerFactory.getLogger(DANSpectrometer.class);
    private             int                             lifeSpan       = 0;
    private             Rover                           rover          = null;
    private             List<DanRDRData.DANDerivedData> danDerivedData = null;

    public DANSpectrometer(Rover rover) {
        this.rover = rover;
        danDerivedData = new ArrayList<>();
    }

    public DanRDRData.DANDerivedData scanForWater() {
        return SensorUtil.getClosestDanRecord(danDerivedData, (long) rover.getSpacecraftClock().getEphemerisTime());
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

    /**
     * This should be fired from the rover during sol Change.
     *
     * @param sol
     */
    public void calibrateDanSensor(int sol) {
        logger.info("Commencing calibration for DAN Spectrometer for sol = " + sol);
        DANCalibrationService danCalibrationService = new DANCalibrationService(sol);
        try {
            this.danDerivedData = danCalibrationService.getDanPayload();
        } catch (IOException e) {
            logger.info("Encountered exception when calibrating DAN Spectrometer, DANDerivedRDRData may not be " +
                        "available for sol = " + sol, e);
        }
    }
}
