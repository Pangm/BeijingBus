package com.widgets;

import java.util.List;

import com.Bus;

import processing.core.PApplet;

public class Button implements Control {
	List<Bus> buses = null;
	boolean isSelected = false;
	float xpos, ypos;
	float width, height;
	boolean isOver = false;
	boolean isDisplay = false;
	PApplet context;

	public Button(PApplet context, float xpos, float ypos, float width,
			float height, List<Bus> buses) {
		this.context = context;
		this.buses = buses;
		this.xpos = xpos;
		this.ypos = ypos;
		this.width = width;
		this.height = height;
	}

	@Override
	public boolean overEvent() {
		if (context.mouseX > xpos && context.mouseX < xpos + width
				&& context.mouseY > ypos && context.mouseY < ypos + height) {
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
//		if (context.mousePressed && isOver) {
		if (isDisplay && isOver) {
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
			context.fill(0, 80);
			// rect(xpos, ypos, width, width);
			context.fill(196, 196, 196, 80);
			context.ellipse(xpos + width / 2, ypos + width / 2, width, width);

			context.fill(255);
			context.ellipse(xpos + width / 2, ypos + width / 2, width / 2,
					width / 2);
			if (isSelected) {
				context.fill(255, 196, 13);
				context.ellipse(xpos + width / 2, ypos + width / 2, width / 2,
						width / 2);
			}
			context.fill(255);
			context.textSize(10);
			context.text("Bus Number", xpos - width, ypos + 3 * height / 2);
		}
	}

	@Override
	public void setIsDisplay(boolean isDisplay) {
		this.isDisplay = isDisplay;
	}

}
