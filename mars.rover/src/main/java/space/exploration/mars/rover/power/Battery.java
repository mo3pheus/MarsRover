package space.exploration.mars.rover.power;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.utils.RoverUtil;

public class Battery {
    private int primaryPowerUnits;
    private int auxiliaryPowerUnits;
    private int alertThreshold;
    private int rechargeTime;

    private Logger logger = LoggerFactory.getLogger(Battery.class);

    public int getPrimaryPowerUnits() {
        return primaryPowerUnits;
    }

    public void setPrimaryPowerUnits(int primaryPowerUnits) {
        this.primaryPowerUnits = primaryPowerUnits;
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
        }

        return powerAvailable;
    }

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

    public int getRechargeTime() {
        return rechargeTime;
    }

    public int getAlertThreshold() {
        return alertThreshold;
    }
}
