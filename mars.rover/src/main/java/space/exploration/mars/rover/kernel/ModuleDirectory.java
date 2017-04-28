package space.exploration.mars.rover.kernel;

public class ModuleDirectory {
	public enum Module {
		SENSOR_LIDAR(0), PROPULSION(1), COMS(2), DIAGNOSTICS(3), POWER(4), SCIENCE(5), KERNEL(6);

		int value;

		Module(int val) {
			value = val;
		}

		public int getValue() {
			return value;
		}
	}
}
