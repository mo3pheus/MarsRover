package space.exploration.mars.rover.power;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.kernel.IsEquipment;
import space.exploration.mars.rover.utils.RoverUtil;

import java.util.concurrent.Semaphore;

public class Battery implements IsEquipment {
    public static final String LIFESPAN = "mars.rover.battery.lifeSpan";
    private int primaryPowerUnits;
    private int auxiliaryPowerUnits;
    private int alertThreshold;
    private int rechargeTime;
    private int lifeSpan;
    private final Semaphore accessLock = new Semaphore(1, true);

    private Logger logger = LoggerFactory.getLogger(Battery.class);

    public Battery(int alertThreshold, int rechargeTime) {
        this.alertThreshold = alertThreshold;
        this.rechargeTime = rechargeTime;
        this.primaryPowerUnits = 10000;
        this.auxiliaryPowerUnits = 1000;
        RoverUtil.roverSystemLog(logger, "Battery configured, batteryLife:primaryPowerUnits = " + primaryPowerUnits
                                         + " auxiliaryPowerUnits = " + auxiliaryPowerUnits + " alertThreshold: " +
                                         alertThreshold + " rechargeTime = " + rechargeTime, "INFO"
        );
    }

    public int getPrimaryPowerUnits() {
        return primaryPowerUnits;
    }

    public void setPrimaryPowerUnits(int primaryPowerUnits) {
        try {
            accessLock.acquire();
        } catch (InterruptedException e) {
            logger.error(" Battery had trouble in acquiring accessLock.", e);
        }
        this.primaryPowerUnits = primaryPowerUnits;
        accessLock.release();
    }

    public int getAuxiliaryPowerUnits() {
        return auxiliaryPowerUnits;
    }

    public void setAuxiliaryPowerUnits(int auxiliaryPowerUnits) {
        this.auxiliaryPowerUnits = auxiliaryPowerUnits;
    }

    public boolean requestPower(int powerUnitsRequested, boolean critical) {
        boolean powerAvailable = false;
        if (!critical) {
            powerAvailable = (primaryPowerUnits - auxiliaryPowerUnits) > powerUnitsRequested;
        } else {
            powerAvailable = (primaryPowerUnits > powerUnitsRequested);
        }

        if (!powerAvailable) {
            logger.error("Battery insufficient at time = " + System.currentTimeMillis() + " Power requested = " +
                         powerUnitsRequested + " Critical = " + critical);
            lifeSpan--;
        }

        return powerAvailable;
    }

    public int getRechargeTime() {
        return rechargeTime;
    }

    public int getAlertThreshold() {
        return alertThreshold;
    }

    public int getLifeSpan() {
        return lifeSpan;
    }

    public void setLifeSpan(int lifeSpan) {
        this.lifeSpan = lifeSpan;
    }

    @Override
    public String getEquipmentName() {
        return "Battery";
    }
}
