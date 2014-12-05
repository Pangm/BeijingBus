import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import processing.core.PApplet;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;

/**
 * Beijing Bus.
 * Visualization of Bus position.
 * .
 */
public class BeijingBus extends PApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	UnfoldingMap map;
	final String DATA_DIRECTORY = "data"; 
	Location beijingLocation = new Location(39.9f, 116.3f);
	List<Bus> buses = new ArrayList<Bus>();
	List<String> lineNums = new ArrayList<String>();
	List<Location> pathLocs = null;
	HScrollbar hs = null; // the scrollbar
	Scrollbar s = null; // the scrollbar
	Button button = null;
	ControlPanel panel = null;
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
						buses.add(new Bus(name.substring(0, name.indexOf('.')), pathLocs, buses));
						String lineNum = name.substring(name.indexOf('-'), name.indexOf('.'));
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

		String mbTilesString = sketchPath("bj_ed2114.mbtiles");

		map = new UnfoldingMap(this, new MBTilesMapProvider(mbTilesString));

		map.zoomToLevel(initZoomLevel);
		map.panTo(beijingLocation);
		map.setZoomRange(9, 14); // prevent zooming too far out
		map.setPanningRestriction(beijingLocation, 50);
		MapUtils.createDefaultEventDispatcher(this, map);

		initBuses(buses, DATA_DIRECTORY);

//		hs = new HScrollbar(4 * width / 5, 3 * height / 8, 20, height / 4, 20, buses);
//		s = new Scrollbar(width / 4, 7 * height / 8 + 40, width / 2, 20, 20, buses);
		s = new Scrollbar(width / 2, 7 * height / 8, width / 3, 20, 20, buses);
		
		button = new Button(6 * width / 7, 7 * height / 8 - 10, 20, 20, buses);
		panel = new ControlPanel(1* width / 20, 4 * height / 5, 9* width / 10, 3 * height / 20, buses);
		
//		controls.add(hs);
		controls.add(panel);
		controls.add(s);
		controls.add(button);
		
		
		Circle grayCircle = new Circle(1* width / 20 + 60, 4 * height / 5 + 40, 30, 30, 1);
		Circle yellowCircle = new Circle(1* width / 20 + 100, 4 * height /5+ 40, 30, 30, 0);
		controls.add(grayCircle);
		controls.add(yellowCircle);
		
		int half = (lineNums.size() + 1)/2;
		
		for (int i = 0; i < half; i++) {
			CircleButton cButton = new CircleButton(1* width / 20 + 150 + i*35, 4 * height / 5 + 20, 
					25, 25, buses, lineNums.get(i));
			controls.add(cButton);
		}
		
		for (int i = half; i < lineNums.size(); i++) {
			CircleButton cButton = new CircleButton(1* width / 20 + 150 + (i-half)*35, 19 * height / 20 - 30, 
					25, 25, buses, lineNums.get(i));
			controls.add(cButton);
		}
		
		frameRate(60);
//		noStroke();
	}

	public void draw() {
		background(0);
		map.draw();

//		hs.update();
//		s.update();
//		hs.display();
//		s.display();
//		button.update();
//		button.display();
//		panel.display();
		
		for (Control control : controls) {
			control.update();
			control.display();
		}
		
		for (Bus bus : buses) {
			bus.display();
		}
		
		if(mousePressed) {
			for (Bus bus : buses) {
				bus.clearPreviosPos();
			}
			
			for (Control control : controls) {
				control.setIsDisplay(true);
			}
			displayFrameCnt = 100;
		} 
		
		if (displayFrameCnt > 0){
			displayFrameCnt--;
		} else {
			for (Control control : controls) {
				control.setIsDisplay(false);
			}
		}
	}
	

	class Bus {
		String name = null;
		List<ScreenPosition> previousPoss = null;
		List<Location> pathLocs = null;
		int index = -1;
		List<Bus> buses = null;
		boolean isCollision = false;
		float progress = 0f;
		boolean isDisplayName = false;
		boolean isDisplay = true;

		float pct = 0;
		float initStep = 0.5f;
		float step = 0.5f;
		ScreenPosition beginPos = null;
		ScreenPosition endPos = null;

		Bus(String name, List<Location> pathLocs, List<Bus> buses) {
			this.name = name;
			this.pathLocs = pathLocs;
			this.buses = buses;
			if (!pathLocs.isEmpty()) {
				index = 0;
			}
			previousPoss = new ArrayList<ScreenPosition>();
			update();
			update();
		}

		private void update() {
			pct = 0;
			ScreenPosition pos = map.getScreenPosition(this.getCurrentLoc());
			beginPos = endPos;

			endPos = pos;

			index += 1;
			index %= pathLocs.size() - 1;
		}

		public void display() {
			
			float x = 0f;
			float y = 0f;
			
			step = initStep / pow(2,(map.getZoomLevel() - initZoomLevel));
			pct += step;

			if (pct < 1.0) {
				x = beginPos.x + (pct * (endPos.x - beginPos.x));
				y = beginPos.y + (pct * (endPos.y - beginPos.y));
			} else {
				x = endPos.x;
				y = endPos.y;
				update();
			}
			addPosition(new ScreenPosition(x, y));
			
			if (isDisplay) {
				int size = previousPoss.size();	
				
				ScreenPosition pos = previousPoss.get(size - 1);
				
				noStroke();
				fill(255, 255, 255,0.2f * 255);
				ellipse(pos.x, pos.y, 10,10);
				
				fill(255, 255, 255);
				ellipse(pos.x, pos.y,5,5);
				
				for (int i = size - 1; i > -1; i--) {
					fill(45,137,239, pow(2, (1 + i - size) * 0.1f) * 255);
//					fill(45,137,239, pow(2, (1 + i - size) * 0.1f) * 150);
					pos = previousPoss.get(i);
					ellipse(pos.x, pos.y,2,2);
					
							
				}
				if (isDisplayName) {
					pos = previousPoss.get(size - 1);
					fill(255);
					text(this.name, pos.x-25, pos.y-10);
				}
			}
			
		}
		
		public void setIsdisplayName(boolean isDisplayName) {
			this.isDisplayName = isDisplayName;
		}
		
		public void setIsdisplay(boolean isDisplay) {
			this.isDisplay = isDisplay;
		}
		
		public Location getCurrentLoc() {
			if (index >= 0 && index <= pathLocs.size()) {
				return pathLocs.get(index);
			} else {
				return null;
			}
		}

		public String getName(){
			return this.name;
		}
		
		public void setProgress(float progress) {
			if (0 <= progress) {
				this.progress = progress;
				this.previousPoss.clear();
				this.index = (int) (this.pathLocs.size() * progress);
				this.previousPoss.clear();
			}
		}
		
		public boolean isCollision() {
			return false;
		}
		
		public void clearPreviosPos() {
			previousPoss.clear();
		}
		
		public void setVelocity(float velocity) {
			if (velocity > 0 && velocity <= 100) {
				initStep *= velocity * 0.6f;
			}
		}

		private void addPosition(ScreenPosition pos) {
			int max = 40;
			if (previousPoss.size() > max) {
				previousPoss.remove(previousPoss.get(0));
			} else {
				// nothing
			}
			previousPoss.add(pos);
		}

	}

	class Scrollbar implements Control {
		int swidth, sheight; // width and height of bar
		float xpos, ypos; // x and y position of bar
		float spos, newspos; // x position of slider
		float sposMin, sposMax; // max and min values of slider
		int loose; // how loose/heavy
		boolean over; // is the mouse over the slider?
		boolean locked;
		float ratio;
		List<Bus> buses = null;
		
		boolean isDisplay = false;

		Scrollbar(float xp, float yp, int sw, int sh, int l, List<Bus> buses) {
			swidth = sw;
			sheight = sh;
			int widthtoheight = sw - sh;
			ratio = (float) sw / (float) widthtoheight;
			xpos = xp;
			ypos = yp - sheight / 2;
			spos = xpos + swidth / 2 - sheight / 2;
			newspos = spos;
			sposMin = xpos;
			sposMax = xpos + swidth - sheight;
			loose = l;
			this.buses = buses;
		}
		
		@Override
		public void setIsDisplay(boolean isDisplay) {
			this.isDisplay = isDisplay;
		}

		@Override
		public void update() {
			if (overEvent()) {
				over = true;
			} else {
				over = false;
			}
			if (mousePressed && over) {
				locked = true;
			}
			if (!mousePressed) {
				locked = false;
			}
			if (locked) {
				newspos = constrain(mouseX - sheight / 2, sposMin, sposMax);
				for (Bus bus : buses) {
					bus.setProgress((spos-sposMin)/swidth);
				}
			}
			if (abs(newspos - spos) > 1) {
				spos = spos + (newspos - spos) / loose;
			}
		}

		float constrain(float val, float minv, float maxv) {
			return min(max(val, minv), maxv);
		}

		@Override
		public boolean overEvent() {
			if (mouseX > xpos && mouseX < xpos + swidth && mouseY > ypos
					&& mouseY < ypos + sheight) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void display() {
			if (isDisplay) {
				strokeWeight(6);
				stroke(50);
				line(xpos, ypos + sheight/2, xpos+swidth, ypos + sheight/2);
				
				fill(255,255,255);
				stroke(255,255,255);
				line(xpos, ypos + sheight/2, spos + sheight/4, ypos + sheight/2);
				
				noStroke();
//				fill(0, 80);
//				rect(xpos, ypos, swidth, sheight);
//				fill(45,137,239);
				
				if (over || locked) {
					fill(255,196,13);
				} else {
					fill(255,255,255);
				}
				ellipse(spos+sheight/2, ypos+sheight/2, sheight/2, sheight/2);
				
				fill(255,255,255);
				text("Timeline", xpos-55, ypos+13);
			}
		}

		float getPos() {
			// Convert spos to be values between
			// 0 and the total width of the scrollbar
			return spos * ratio;
		}
	}
	
	class HScrollbar implements Control {
		int swidth, sheight; // width and height of bar
		float xpos, ypos; // x and y position of bar
		float spos, newspos; // x position of slider
		float sposMin, sposMax; // max and min values of slider
		int loose; // how loose/heavy
		boolean over; // is the mouse over the slider?
		boolean locked;
		float ratio;
		List<Bus> buses = null;
		
		boolean isDisplay = false;

		HScrollbar(float xp, float yp, int sw, int sh, int l, List<Bus> buses) {
			swidth = sw;
			sheight = sh;
			int widthtoheight = sw - sh;
			ratio = (float) sw / (float) widthtoheight;
			xpos = xp;
			ypos = yp - swidth / 2;
			spos = ypos + sheight / 2 - swidth / 2;
			newspos = spos;
			sposMin = ypos;
			sposMax = ypos + sheight - swidth;
			loose = l;
			this.buses = buses;
		}
		
		@Override
		public void setIsDisplay(boolean isDisplay) {
			this.isDisplay = isDisplay;
		}

		@Override
		public void update() {
			if (overEvent()) {
				over = true;
			} else {
				over = false;
			}
			if (mousePressed && over) {
				locked = true;
			}
			if (!mousePressed) {
				locked = false;
			}
			if (locked) {
				newspos = constrain(mouseY - swidth / 2, sposMin, sposMax);
				for (Bus bus : buses) {
					bus.setVelocity((sposMax - spos) / sheight);
				}
			}
			if (abs(newspos - spos) > 1) {
				spos = spos + (newspos - spos) / loose;
			}
		}

		float constrain(float val, float minv, float maxv) {
			return min(max(val, minv), maxv);
		}

		@Override
		public boolean overEvent() {
			if (mouseX > xpos && mouseX < xpos + swidth && mouseY > ypos
					&& mouseY < ypos + sheight) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void display() {
			if (isDisplay) {
				stroke(50,80);
				line(xpos + swidth/2, ypos, xpos + swidth/2, ypos + sheight - swidth/2);
				
				stroke(255,255,255);
				line(xpos + swidth/2, spos + swidth*3/4, xpos + swidth/2, ypos + sheight);
				
				noStroke();
				fill(0, 80);
				rect(xpos, ypos, swidth, sheight);
				fill(45,137,239);
				
				if (over || locked) {
					fill(255,255,255);
				} else {
					fill(255,255,255, 150);
				}
//				stroke(255);
				ellipse(xpos + swidth/2, spos + swidth/2, swidth/2, swidth/2);
				
				fill(50);
				text("Speed", xpos-10, ypos-5);
			}
		}
		
		float getPos() {
			// Convert spos to be values between
			// 0 and the total width of the scrollbar
			return spos * ratio;
		}
	}
	
	class Button implements Control {
		List<Bus> buses = null;
		boolean isSelected = false;
		float xpos, ypos;
		float width, height;
		boolean isOver = false;
		boolean isDisplay = false;
		
		Button(float xpos, float ypos, float width, float height, List<Bus> buses) {
			this.buses = buses;
			this.xpos = xpos;
			this.ypos = ypos;
			this.width = width;
			this.height = height;
		}
		
		@Override
		public boolean overEvent() {
			if (mouseX > xpos && mouseX < xpos + width && mouseY > ypos
					&& mouseY < ypos + height) {
				return true;
			} else {
				return false;
			}
		}
		
		@Override
		public void update() {
			if (overEvent()) {
				isOver = true;
			} else {
				isOver = false;
			}
			if (mousePressed && isOver) {
				isSelected = !isSelected;
				if (isSelected) {
					for (Bus bus : buses) {
						bus.setIsdisplayName(true);
					}
				} else {
					for (Bus bus : buses) {
						bus.setIsdisplayName(false);
					}
				}
			}
		}
		
		@Override
		public void display() {
			if (isDisplay) {				
				fill(0, 80);
//				rect(xpos, ypos, width, width);
				fill(196,196,196, 80);
				ellipse(xpos + width/2, ypos + width/2, width, width);
				
				fill(255);
				ellipse(xpos + width/2, ypos + width/2, width/2, width/2);
				if (isSelected) {
					fill(255,196,13);
					ellipse(xpos + width/2, ypos + width/2, width/2, width/2);
				}
				fill(255);
				textSize(10);
				text("Bus Number", xpos-width, ypos+3*height/2);
			}
		}
		
		@Override
		public void setIsDisplay(boolean isDisplay) {
			this.isDisplay = isDisplay;
		}
		
	}
	
	class ControlPanel implements Control {
		List<Bus> buses = null;
		boolean isSelected = false;
		float xpos, ypos;
		float width, height;
		boolean isOver = false;
		boolean isDisplay = false;
		
		ControlPanel(float xpos, float ypos, float width, float height, List<Bus> buses) {
			this.buses = buses;
			this.xpos = xpos;
			this.ypos = ypos;
			this.width = width;
			this.height = height;
		}
		
		@Override
		public boolean overEvent() {
			if (mouseX > xpos && mouseX < xpos + width && mouseY > ypos
					&& mouseY < ypos + height) {
				return true;
			} else {
				return false;
			}
		}
		
		@Override
		public void update() {
		}
		
		@Override
		public void display() {
			if (this.isDisplay) {				
				fill(0,0,0, 0.65f * 255);
				rect(xpos, ypos, width, height);
			}
		}
		
		@Override
		public void setIsDisplay(boolean isDisplay) {
			this.isDisplay = isDisplay;
		}
	}
	
	class CircleButton implements Control {
		List<Bus> buses = null;
		boolean isSelected = false;
		float xpos, ypos;
		float width, height;
		boolean isOver = false;
		boolean isDisplay = false;
		String lineNum = "";
		
		CircleButton(float xpos, float ypos, float width, float height, List<Bus> buses, String lineNum) {
			this.buses = buses;
			this.xpos = xpos;
			this.ypos = ypos;
			this.width = width;
			this.height = height;
			this.lineNum = lineNum;
			isSelected = true;
		}
		
		@Override
		public boolean overEvent() {
			float dis = pow((mouseX - xpos), 2) + pow((mouseY-ypos), 2);
			if (dis <= pow(this.width / 2, 2)) {
				return true;
			} else {
				return false;
			}
		}
		
		@Override
		public void update() {
			if (overEvent()) {
				isOver = true;
			} else {
				isOver = false;
			}
			if (mousePressed && isOver) {
				isSelected = !isSelected;
				if (isSelected) {
					for (Bus bus : buses) {
						if (bus.name.contains(lineNum)) {
							bus.setIsdisplay(true);
						}
					}
				} else {
					for (Bus bus : buses) {
						if (bus.name.contains(lineNum)) {
							bus.setIsdisplay(false);
						}
					}
				}
			}
		}
		
		@Override
		public void display() {
			if (isDisplay) {
				strokeWeight(1);
				if (isSelected) {
					noStroke();
					fill(255,196,13);
					ellipse(xpos, ypos, width, height);
				} else {
					noStroke();
					fill(196,196,196,50);
					ellipse(xpos, ypos, width, height);
				}
				fill(255);
				textSize(10);
				text(lineNum.substring(1), xpos-width/2, ypos+height);
			}
		}
		
		@Override
		public void setIsDisplay(boolean isDisplay) {
			this.isDisplay = isDisplay;
		}
		
	}
	
	class Circle implements Control{
		float xpos, ypos;
		float width, height;
		boolean isDisplay = false;
		int type;
		
		Circle(float xpos, float ypos, float width, float height, int type) {
			this.type = type;
			this.xpos = xpos;
			this.ypos = ypos;
			this.width = width;
			this.height = height;
		}
		
		@Override
		public boolean overEvent() {
			if (mouseX > xpos && mouseX < xpos + width && mouseY > ypos
					&& mouseY < ypos + height) {
				return true;
			} else {
				return false;
			}
		}
		
		@Override
		public void update() {
		}
		
		@Override
		public void display() {
			strokeWeight(1);
			if(this.isDisplay) {
				fill(255);
				textSize(10);
				if (this.type == 0) {
					text("Hidden", xpos-width/2, ypos+height);
					noStroke();
					fill(196,196,196,50);
					ellipse(xpos, ypos, width, height);
				} else {
					text("Display", xpos-width/2, ypos+height);
					noStroke();
					fill(255,196,13);
					ellipse(xpos, ypos, width, height);
				}
				
			} 
		}

		@Override
		public void setIsDisplay(boolean isDisplay) {
			this.isDisplay = isDisplay;
		}
	}
	
	interface Control {
		boolean overEvent();
		void update();
		void display();
		void setIsDisplay(boolean isDisplay);
	}
}
