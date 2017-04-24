package space.exploration.mars.rover.kernel;

public class Battery {
	private int	primaryPowerUnits;
	private int	auxillaryPowerUnits;

	public int getPrimaryPowerUnits() {
		return primaryPowerUnits;
	}

	public void setPrimaryPowerUnits(int primaryPowerUnits) {
		this.primaryPowerUnits = primaryPowerUnits;
	}

	public int getAuxillaryPowerUnits() {
		return auxillaryPowerUnits;
	}

	public void setAuxillaryPowerUnits(int auxillaryPowerUnits) {
		this.auxillaryPowerUnits = auxillaryPowerUnits;
	}

	public boolean requestPower(int powerUnitsRequested, boolean critical) {
		if (!critical) {
			return (primaryPowerUnits - auxillaryPowerUnits) > powerUnitsRequested;
		} else {
			return (primaryPowerUnits > powerUnitsRequested);
		}
	}
}
