package com.widgets;

import java.util.List;

import processing.core.PApplet;

import com.Bus;

public class CircleButton implements Control {
	List<Bus> buses = null;
	boolean isSelected = false;
	float xpos, ypos;
	float width, height;
	boolean isOver = false;
	boolean isDisplay = false;
	String lineNum = "";
	PApplet context;

	public CircleButton(PApplet context, float xpos, float ypos, float width,
			float height, List<Bus> buses, String lineNum) {
		this.context = context;
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
		float dis = PApplet.pow((context.mouseX - xpos), 2)
				+ PApplet.pow((context.mouseY - ypos), 2);
		if (dis <= PApplet.pow(this.width / 2, 2)) {
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
		if (context.mousePressed && isOver) {
			isSelected = !isSelected;
			if (isSelected) {
				for (Bus bus : buses) {
					if (bus.getName().contains(lineNum)) {
						bus.setIsdisplay(true);
					}
				}
			} else {
				for (Bus bus : buses) {
					if (bus.getName().contains(lineNum)) {
						bus.setIsdisplay(false);
					}
				}
			}
		}
	}

	@Override
	public void display() {
		if (isDisplay) {
			context.strokeWeight(1);
			if (isSelected) {
				context.noStroke();
				context.fill(255, 196, 13);
				context.ellipse(xpos, ypos, width, height);
			} else {
				context.noStroke();
				context.fill(196, 196, 196, 50);
				context.ellipse(xpos, ypos, width, height);
			}
			context.fill(255);
			context.textSize(10);
			context.text(lineNum.substring(1), xpos - width / 2, ypos + height);
		}
	}

	@Override
	public void setIsDisplay(boolean isDisplay) {
		this.isDisplay = isDisplay;
	}

}
