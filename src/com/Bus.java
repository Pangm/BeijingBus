package com;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.utils.ScreenPosition;

public class Bus {
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
	PApplet context;
	UnfoldingMap map;
	int initZoomLevel;

	public Bus(PApplet context, UnfoldingMap map, int initZoomLevel, String name, List<Location> pathLocs, List<Bus> buses) {
		this.context = context;
		this.map = map;
		this.initZoomLevel = initZoomLevel;
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
		
		step = initStep / PApplet.pow(2,(map.getZoomLevel() - initZoomLevel));
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
			
			context.noStroke();
			context.fill(255, 255, 255,0.2f * 255);
			context.ellipse(pos.x, pos.y, 10,10);
			
			context.fill(255, 255, 255);
			context.ellipse(pos.x, pos.y,5,5);
			
			for (int i = size - 1; i > -1; i--) {
				context.fill(45,137,239, PApplet.pow(2, (1 + i - size) * 0.1f) * 255);
//				fill(45,137,239, pow(2, (1 + i - size) * 0.1f) * 150);
				pos = previousPoss.get(i);
				context.ellipse(pos.x, pos.y,2,2);
				
						
			}
			if (isDisplayName) {
				pos = previousPoss.get(size - 1);
				context.fill(255);
				context.text(this.name, pos.x-25, pos.y-10);
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
