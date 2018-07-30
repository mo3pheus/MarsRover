package space.exploration.mars.rover.service.calibration.spectrometer.sam;

import java.util.concurrent.Callable;

public interface IsCalibrationService extends Callable<DataAvailabilityPacket> {
    DataAvailabilityPacket call() throws Exception;
}
