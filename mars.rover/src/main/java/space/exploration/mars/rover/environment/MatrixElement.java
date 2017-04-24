package space.exploration.mars.rover.environment;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.util.Properties;

public abstract class MatrixElement extends Component {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 2873464931715299868L;
	private Properties			matrixConfig		= null;

	public abstract void draw(Graphics2D canvas);

	public abstract Color getColor();

	public void setProperties(Properties matrixConfig) {
		this.matrixConfig = matrixConfig;
	}

	public Properties getMatrixConfig() {
		return matrixConfig;
	}

	public void setMatrixConfig(Properties matrixConfig) {
		this.matrixConfig = matrixConfig;
	}

	public void setLayout() {
		if (matrixConfig != null) {
			int frameHeight = Integer.parseInt(this.matrixConfig.getProperty(EnvironmentUtils.FRAME_HEIGHT_PROPERTY));
			int frameWidth = Integer.parseInt(this.matrixConfig.getProperty(EnvironmentUtils.FRAME_WIDTH_PROPERTY));

			this.setBounds(0, 0, frameWidth, frameHeight);
		}
	}
}
