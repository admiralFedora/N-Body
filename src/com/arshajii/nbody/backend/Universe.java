package com.arshajii.nbody.backend;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.microedition.khronos.opengles.GL10;

import com.admiralFedora.n_body.MyGLRenderer;
import com.admiralFedora.n_body.globalVar;

import android.util.Log;

public class Universe {
	private final CopyOnWriteArrayList<Body> bodies = new CopyOnWriteArrayList<Body>();

	private int time = 0;

	public static double gravConst = Const.G;

	boolean walled = false;
	private double xWallUp, xWallDown;
	private double yWallUp, yWallDown;

	// private final GL10 gl;

	public Universe() {

	}

	public Universe(Collection<Body> bodies) {
		this.bodies.addAll(bodies);
	}

	public Universe(Body[] bodies) {
		this.bodies.addAll(Arrays.asList(bodies));
	}

	public void addBody(float xPos, float yPos, float xVel, float yVel,
			float SCALE, float R, float G, float B, boolean isStatic) {
		synchronized (bodies) {

			bodies.add(new Body(new Vec(xPos, yPos), new Vec(xVel, yVel), SCALE
					* globalVar.massSCALE, SCALE, R, G, B, isStatic));
		}
	}

	public void removeBody() {
		// Log.d("attempt to remove",String.valueOf(this.bodies.size()));
		bodies.remove(bodies.size() - 1);
		// Log.d("removed",String.valueOf(this.bodies.size()));
	}

	public void step(GL10 gl) {
		synchronized (bodies) {
			for (Body body1 : bodies) {

				body1.draw(gl);
				MutableVec netForce = new MutableVec(0, 0);

				for (Body body2 : bodies) {

					if (body1 != body2) {
						netForce.inPlaceAdd(body1.forceFrom(body2));
						if (globalVar.realism) {
							double d = body1.distance(body2)
									* globalVar.distSCALEDOWN;
							if (d <= body2.size) {
								body2.absorption(body1);
								bodies.remove(body1);

							}
						}
					}
				}

				body1.move(netForce);

				Vec pos = body1.getPos();

				if (pos.getX() >= xWallUp || pos.getX() <= xWallDown) {
					body1.reverseVx();
				}

				if (pos.getY() >= yWallUp || pos.getY() <= yWallDown) {
					body1.reverseVy();
				}

			}
		}

		time++;
	}

	public void refactor(float massCHNG, float distCHNG, float velCHNG) {
		synchronized (bodies) {
			for (Body body : bodies) {
				body.refactor(massCHNG, distCHNG, velCHNG);
			}
		}
	}

	public void clear() {
		bodies.clear();
	}

	public void moveAll(Vec delta) {
		synchronized (bodies) {
			for (Body body : bodies)
				body.moveDirect(delta);
		}
	}

	public void addWalls(double xWallUp, double xWallDown, double yWallUp,
			double yWallDown) {

		this.xWallUp = xWallUp;
		this.xWallDown = xWallDown;
		this.yWallUp = yWallUp;
		this.yWallDown = yWallDown;
		
		Log.d("walls",String.valueOf(yWallUp));

		walled = true;
	}

	public void print(Writer w) {
		try {
			w.write(Integer.toString(time));
			w.write('\t');

			for (Body body : bodies) {
				w.write(Double.toString(body.getPos().getX()));
				w.write('\t');
				w.write(Double.toString(body.getPos().getY()));
				w.write('\t');
			}

			w.write('\n');
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void print() {
		System.out.print(time);
		System.out.print('\t');

		for (Body body : bodies) {
			System.out.print(body.getPos().getX());
			System.out.print('\t');
			System.out.print(body.getPos().getY());
			System.out.print('\t');
		}

		System.out.println();
	}

	public boolean empty() {
		return bodies.isEmpty();
	}

	public void draw(GL10 gl) {
		synchronized (bodies) {
			for (Body body1 : bodies) {
				body1.draw(gl);
			}
		}
	}

}
