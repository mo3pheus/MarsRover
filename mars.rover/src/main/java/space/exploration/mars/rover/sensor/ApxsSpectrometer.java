package space.exploration.mars.rover.sensor;

import com.yammer.metrics.core.Meter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.mars.rover.kernel.Rover;
import space.exploration.mars.rover.sensors.apxs.ApxsData;
import space.exploration.mars.rover.service.ApxsDataService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by sanketkorgaonkar on 5/2/17.
 */
public class ApxsSpectrometer implements IsEquipment {
    private Logger                  logger          = LoggerFactory.getLogger(ApxsSpectrometer.class);
    private Meter                   requests        = null;
    private int                     lifeSpan        = 1000;
    private ApxsDataService         apxsDataService = null;
    private ApxsData.ApxsDataPacket apxsDataPacket  = null;
    private Rover                   rover           = null;

    public ApxsSpectrometer(Rover rover) {
        this.rover = rover;
        requests = this.rover.getMetrics().newMeter(ApxsSpectrometer.class, "ApxsSpectrometer", "requests", TimeUnit
                .HOURS);
    }

    public void calibrateApxsSpectrometer(int sol) {
        try {
            apxsDataService = new ApxsDataService(sol, rover.getDataArchiveLocation() + "/APXS/");
            apxsDataPacket = apxsDataService.getApxsDataPacket();
        } catch (IOException e) {
            apxsDataPacket = null;
            logger.error("ApxsData not available for sol = " + sol, e);
        }
    }

    public ApxsData.ApxsDataPacket getApxsDataPacket() {
        requests.mark();
        if (apxsDataPacket != null) {
            lifeSpan--;
        }
        return apxsDataPacket;
    }

    @Override
    public int getLifeSpan() {
        return lifeSpan;
    }

    @Override
    public String getEquipmentName() {
        return "ApxsSpectrometer";
    }

    @Override
    public boolean isEndOfLife() {
        return (lifeSpan > 0);
    }

    @Override
    public long getRequestMetric() {
        return requests.count();
    }
}
