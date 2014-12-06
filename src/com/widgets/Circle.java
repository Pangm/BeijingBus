package com.widgets;

import processing.core.PApplet;

public class Circle implements Control {
	float xpos, ypos;
	float width, height;
	boolean isDisplay = false;
	int type;
	PApplet context;

	public Circle(PApplet context, float xpos, float ypos, float width,
			float height, int type) {
		this.context = context;
		this.type = type;
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
	}

	@Override
	public void display() {
		context.strokeWeight(1);
		if (this.isDisplay) {
			context.fill(255);
			context.textSize(10);
			if (this.type == 0) {
				context.text("Hidden", xpos - width / 2, ypos + height);
				context.noStroke();
				context.fill(196, 196, 196, 50);
				context.ellipse(xpos, ypos, width, height);
			} else {
				context.text("Display", xpos - width / 2, ypos + height);
				context.noStroke();
				context.fill(255, 196, 13);
				context.ellipse(xpos, ypos, width, height);
			}

		}
	}

	@Override
	public void setIsDisplay(boolean isDisplay) {
		this.isDisplay = isDisplay;
	}
}