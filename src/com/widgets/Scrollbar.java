package com.widgets;

import java.util.List;

import processing.core.PApplet;

import com.Bus;

public class Scrollbar implements Control {
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
	PApplet context;

	public Scrollbar(PApplet context, float xp, float yp, int sw, int sh,
			int l, List<Bus> buses) {
		swidth = sw;
		sheight = sh;
		int widthtoheight = sw - sh;
		ratio = (float) sw / (float) widthtoheight;
		xpos = xp;
		ypos = yp - sheight / 2;
		spos = xpos + swidth / 4 - sheight / 2;
		newspos = spos;
		sposMin = xpos;
		sposMax = xpos + swidth - sheight;
		loose = l;
		this.buses = buses;
		this.context = context;
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
//		if (context.mousePressed && over) {
//			locked = true;
//		}
//		if (!context.mousePressed) {
//			locked = false;
//		}
		if (over) {
			newspos = constrain(context.mouseX - sheight / 2, sposMin, sposMax);
			for (Bus bus : buses) {
				bus.setProgress((spos - sposMin) / swidth);
			}
		}
//		if (PApplet.abs(newspos - spos) > 1) {
//			spos = spos + (newspos - spos) / loose;
//		}
	}

	float constrain(float val, float minv, float maxv) {
		return PApplet.min(PApplet.max(val, minv), maxv);
	}

	@Override
	public boolean overEvent() {
		if (context.mouseX > xpos && context.mouseX < xpos + swidth
				&& context.mouseY > ypos && context.mouseY < ypos + sheight) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void display() {
		if (isDisplay) {
			if (PApplet.abs(newspos - spos) > 1) {
				spos = spos + (newspos - spos) / loose;
			}
			context.strokeWeight(6);
			context.stroke(50);
			context.line(xpos, ypos + sheight / 2, xpos + swidth, ypos
					+ sheight / 2);

			context.fill(255, 255, 255);
			context.stroke(255, 255, 255);
			context.line(xpos, ypos + sheight / 2, spos + sheight / 4, ypos
					+ sheight / 2);

			context.noStroke();

			if (over || locked) {
				context.fill(255, 196, 13);
			} else {
				context.fill(255, 255, 255);
			}
			context.ellipse(spos + sheight / 2, ypos + sheight / 2,
					sheight / 2, sheight / 2);

			context.fill(255, 255, 255);
			context.text("Timeline", xpos - 55, ypos + 13);
		}
	}

	float getProgress() {
		return (spos - sposMin) / swidth;
	}
	float getPos() {
		// Convert spos to be values between
		// 0 and the total width of the scrollbar
		return spos * ratio;
	}
}
