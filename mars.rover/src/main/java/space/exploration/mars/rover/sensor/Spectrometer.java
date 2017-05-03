package space.exploration.mars.rover.sensor;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import space.exploration.mars.rover.environment.SoilComposition;
import space.exploration.mars.rover.spectrometer.SpectrometerScanOuterClass.SpectrometerScan;
import space.exploration.mars.rover.spectrometer.SpectrometerScanOuterClass.SpectrometerScan.Composition;
import space.exploration.mars.rover.spectrometer.SpectrometerScanOuterClass.SpectrometerScan.Location;
import space.exploration.mars.rover.spectrometer.SpectrometerScanOuterClass.SpectrometerScan.PointComp;

import java.awt.Point;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by sanketkorgaonkar on 5/2/17.
 */
public class Spectrometer {
	public class SoilComp {
		private Point			point;
		private SoilComposition	composition;

		public Point getPoint() {
			return point;
		}

		public void setPoint(Point point) {
			this.point = point;
		}

		public SoilComposition getComposition() {
			return composition;
		}

		public void setComposition(SoilComposition composition) {
			this.composition = composition;
		}

		public SoilComp(Point point, SoilComposition soilComposition) {
			this.point = point;
			this.composition = soilComposition;
		}

		public String toString() {
			return " Point = " + point.toString() + " Composition = " + composition.toString();
		}
	}

	private Logger						logger			= LoggerFactory.getLogger(Spectrometer.class);
	private Point						origin			= null;
	private Map<Point, SoilComposition>	surfaceComp		= null;
	private List<SoilComp>				scanAreaComp	= null;
	private int							cellWidth		= 0;

	public void setSurfaceComp(Map<Point, SoilComposition> surfaceComp) {
		this.surfaceComp = surfaceComp;
	}

	public void setCellWidth(int cellWidth) {
		this.cellWidth = cellWidth;
	}

	public List<SoilComp> getScanAreaComp() {
		return this.scanAreaComp;
	}

	public Spectrometer(Point origin) {
		this.origin = origin;
		scanAreaComp = new ArrayList<SoilComp>();
	}

	public void processSurroundingArea() {
		if (surfaceComp == null || cellWidth == 0) {
			logger.error("SurfaceComp or cellWidth not set for spectrometer");
			return;
		}

		for (int i = ((int)origin.getX() - cellWidth); i <= ((int)origin.getX() + cellWidth); i = (i + cellWidth)) {
			for (int j = ((int)origin.getY() - cellWidth); j <= ((int)origin.getY() + cellWidth); j = (j + cellWidth)) {
				if (i < 0 || j < 0) {
					continue;
				}

				Point temp = new Point(i, j);
				System.out.println(" Point = >" + temp.toString());
				SoilComposition composition = surfaceComp.get(temp);
				SoilComp comp = new SoilComp(temp, composition);
				scanAreaComp.add(comp);
				logger.info(" Processing point = " + temp.toString() + " Composition = " + comp.toString());
			}
		}
	}

	public SpectrometerScan getSpectrometerReading() {
		SpectrometerScan.Builder sBuilder = SpectrometerScan.newBuilder();
		List<PointComp> sampleReadings = new ArrayList<PointComp>();

		for (SoilComp sample : scanAreaComp) {
			PointComp.Builder pBuilder = PointComp.newBuilder();

			Point temp = sample.getPoint();
			pBuilder.setPoint(Location.newBuilder().setX(temp.x).setY(temp.y).build());

			SoilComposition composition = sample.getComposition();
			pBuilder.setSoilComp(Composition.newBuilder().setAl2O3(composition.getAl2O3()).setFeO(composition.getFeO())
					.setMgO(composition.getMgO()).setNa2O(composition.getNa2O()).build());
			sampleReadings.add(pBuilder.build());
		}
		sBuilder.addAllScanAreaComp(sampleReadings);
		return sBuilder.build();
	}
}
