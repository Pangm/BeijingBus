package com;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import com.widgets.*;

import processing.core.PApplet;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;

/**
 * Beijing Bus. Visualization of Bus position. .
 */
public class BeijingBus extends PApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	UnfoldingMap map;
	final String DATA_DIRECTORY = "../data/bus";
	Location beijingLocation = new Location(39.9f, 116.3f);
	List<Bus> buses = new ArrayList<Bus>();
	List<String> lineNums = new ArrayList<String>();
	List<Location> pathLocs = null;
	Scrollbar s = null; // the scrollbar
	Button button = null;
	Panel panel = null;
	int initZoomLevel = 11;

	List<Control> controls = new ArrayList<Control>();

	int displayFrameCnt = 80;

	void loadData(List<Location> list, String filename) {
		BufferedReader reader = createReader(filename);

		for (int i = 0; i < 1600; i += 5) {
			String line;
			try {
				line = reader.readLine();
				while (line != null) {
					String[] pieces = split(line, ',');
					float x = Float.parseFloat(pieces[0]);
					float y = Float.parseFloat(pieces[1]);
					Location loc = new Location(y, x);
					list.add(loc);
					// System.out.println("Location " + x + ", " + y);
					line = reader.readLine();
				}
				// System.out.println(line);
			} catch (IOException e) {
				e.printStackTrace();
				line = null;
			}
		}
	}

	private void initBuses(List<Bus> buses, String filePath) {
		try {
			File file = new File(filePath);
			if (file.isDirectory()) {
				System.out.println(file.getAbsolutePath());
				String[] filelist = file.list();
				for (int i = 0; i < filelist.length; i++) {
					File readfile = new File(filePath + "/" + filelist[i]);
					if (!readfile.isDirectory()) {
						String name = readfile.getName();
						System.out.println("path=" + readfile.getPath());
						System.out.println("absolutepath="
								+ readfile.getAbsolutePath());
						System.out.println("name=" + name);

						pathLocs = new ArrayList<Location>();
						loadData(pathLocs, readfile.getPath());
						buses.add(new Bus(this, map, initZoomLevel, name.substring(0, name.indexOf('.')),
								pathLocs, buses));
						String lineNum = name.substring(name.indexOf('-'),
								name.indexOf('.'));
						if (!lineNums.contains(lineNum)) {
							lineNums.add(lineNum);
						}
					} else {
						// readfile(filePath + "/" + filelist[i]);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("readfile()   Exception:" + e.getMessage());
		}
	}

	public void setup() {
		size(1000, 600, P2D);

		String mbTilesString = sketchPath("../data/map/bj_ed2114.mbtiles");

		map = new UnfoldingMap(this, new MBTilesMapProvider(mbTilesString));

		map.zoomToLevel(initZoomLevel);
		map.panTo(beijingLocation);
		map.setZoomRange(9, 14); // prevent zooming too far out
		map.setPanningRestriction(beijingLocation, 50);
		MapUtils.createDefaultEventDispatcher(this, map);

		initBuses(buses, DATA_DIRECTORY);

		s = new Scrollbar(this, width / 2, 7 * height / 8, width / 3, 20, 20,
				buses);

		button = new Button(this, 6 * width / 7, 7 * height / 8 - 10, 20, 20,
				buses);
		panel = new Panel(this, 1 * width / 20, 4 * height / 5, 9 * width / 10,
				3 * height / 20, buses);

		controls.add(panel);
		controls.add(s);
		controls.add(button);

		Circle grayCircle = new Circle(this, 1 * width / 20 + 60,
				4 * height / 5 + 40, 30, 30, 1);
		Circle yellowCircle = new Circle(this, 1 * width / 20 + 100,
				4 * height / 5 + 40, 30, 30, 0);
		controls.add(grayCircle);
		controls.add(yellowCircle);

		int half = (lineNums.size() + 1) / 2;

		for (int i = 0; i < half; i++) {
			CircleButton cButton = new CircleButton(this, 1 * width / 20 + 150
					+ i * 35, 4 * height / 5 + 20, 25, 25, buses,
					lineNums.get(i));
			controls.add(cButton);
		}

		for (int i = half; i < lineNums.size(); i++) {
			CircleButton cButton = new CircleButton(this, 1 * width / 20 + 150
					+ (i - half) * 35, 19 * height / 20 - 30, 25, 25, buses,
					lineNums.get(i));
			controls.add(cButton);
		}

		frameRate(60);
	}

	public void draw() {
		background(0);
		map.draw();

		for (Control control : controls) {
			control.update();
			control.display();
		}

		for (Bus bus : buses) {
			bus.display();
		}

		if (mousePressed) {
			for (Bus bus : buses) {
				bus.clearPreviosPos();
			}

			for (Control control : controls) {
				control.setIsDisplay(true);
			}
			displayFrameCnt = 100;
		}

		if (displayFrameCnt > 0) {
			displayFrameCnt--;
		} else {
			for (Control control : controls) {
				control.setIsDisplay(false);
			}
		}
	}

}
