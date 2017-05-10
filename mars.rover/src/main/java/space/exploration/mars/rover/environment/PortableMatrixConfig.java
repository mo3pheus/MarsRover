package space.exploration.mars.rover.environment;

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

public class PortableMatrixConfig implements Serializable {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -2277832505186440104L;
	private Properties			matrixConfig		= null;

	public PortableMatrixConfig() {
		try {
			matrixConfig = EnvironmentUtils.getMatrixConfig();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Properties getMatrixConfig() {
		return matrixConfig;
	}

	public void setMatrixConfig(Properties matrixConfig) {
		this.matrixConfig = matrixConfig;
	}
}
