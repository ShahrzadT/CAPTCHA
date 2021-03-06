package traveling;

/*
 * File: Navigation.java
 * Written by: Sean Lawlor
 * ECSE 211 - Design Principles and Methods, Head TA
 * Fall 2011
 *
 * Movement control class (turnTo, travelTo, flt, localize)
 * 
 * Edited by: Alessandro Parisi
 */
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.Sound;

/**
 * The <code>Navigation</code> class will control the movements of the robot. It
 * will use the <code>Odometer</code> and motors to control this movement.
 * 
 * It calculates the minimal angle to turn in all cases when moving to a
 * different point on the grid.
 * 
 * An instance of the <code>Navigation</code> class holds the
 * <code>double</code> error values, the <code>int</code> acceleration, and the
 * <code>int</code> different speeds. It also holds the <code>double</code>
 * width and wheel radius of the robot. In addition, it has a
 * <code>boolean</code> value to monitor when the robot is turning to stop
 * <code>OdometeryCorrection</code> when this is happening.
 * 
 * @see Odometer
 * 
 * @author Alessandro Parisi
 * @author Sean Lawlor
 * 
 */
public class Navigation {
	private final static int FAST = 300, SLOW = 150, ACCELERATION = 4000,
			MID = 250;
	private final static double DEG_ERR = 5.0, CM_ERR = 1.0;
	private final double wheelRadius = -2.1, width = 17.25;
	private Odometer odometer;
	private NXTRegulatedMotor leftMotor, rightMotor;
	private boolean isTurning = false;
	private boolean repeat = false;

	/**
	 * The constructor of this class will initiate the motors and
	 * <code>Odometer</code> and sets the acceleration of the motors.
	 * 
	 * @param odo
	 *            The <code>Odometer</code> that is used for the robot
	 */
	public Navigation(Odometer odo) {
		this.odometer = odo;

		this.leftMotor = Motor.A;
		this.rightMotor = Motor.B;

		// set acceleration
		this.leftMotor.setAcceleration(ACCELERATION);
		this.rightMotor.setAcceleration(ACCELERATION);
	}

	// This method converts the distance traveled given a radius and travel
	// distance
	private int convertDistance(double radius, double travelDis) {
		return (int) ((180.0 * travelDis) / (Math.PI * radius));
	}

	// This method converts the angle traveled given a radius, width, and angle
	private int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

	/**
	 * Functions to set the motor speeds jointly for
	 * 
	 * @param lSpd
	 *            The <code>float</code > speed of the left motor
	 * @param rSpd
	 *            The <code>float</code > speed of the right motor
	 */
	public void setSpeeds(float lSpd, float rSpd) {
		this.leftMotor.setSpeed(Math.abs(lSpd));
		this.rightMotor.setSpeed(Math.abs(rSpd));
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}

	/**
	 * Functions to set the motor speeds jointly for
	 * 
	 * @param lSpd
	 *            The <code>int</code > speed of the left motor
	 * @param rSpd
	 *            The <code>int</code > speed of the right motor
	 */

	public void setSpeeds(int lSpd, int rSpd) {
		this.leftMotor.setSpeed(Math.abs(lSpd));
		this.rightMotor.setSpeed(Math.abs(rSpd));
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}

	/**
	 * Float the two motors jointly
	 */
	public void setFloat() {
		this.leftMotor.stop();
		this.rightMotor.stop();
		this.leftMotor.flt(true);
		this.rightMotor.flt(true);
	}

	/**
	 * TravelTo function which takes as arguments the x and y position in cm
	 * will travel to designated position, while constantly updating it's
	 * heading
	 * 
	 * @param x
	 *            The <code>double</code> x position of the robot
	 * @param y
	 *            The <code>double</code> y position of the robot
	 */
	public void travelTo(double x, double y, boolean immediateRet) {

		double dX, dY, angle, travelDis, check;
		int tries = 0;

		if (repeat)
			repeat = false;
		else
			repeat = true;

		// Get the x y and that we need to travel
		dX = x - odometer.getX();
		dY = y - odometer.getY();

		// find the direction by using tan
		angle = Math.atan2(dY, dX) * 180 / Math.PI;

		// Correct the angle until its good
		while (Math.abs(odometer.getAng() - angle) > DEG_ERR && tries < 10) {
			tries = tries + 1;
			turnTo(angle, true);
		}

		// Make the wheels move slowly
		leftMotor.setSpeed(MID);
		rightMotor.setSpeed(MID);

		// Calcuate the travel distance by using pythagorean
		travelDis = Math.sqrt(dX * dX + dY * dY);

		// Move the robot a certain distance and then stop the motors
		Motor.A.rotate(convertDistance(wheelRadius, travelDis), true);
		Motor.B.rotate(convertDistance(wheelRadius, travelDis), immediateRet);

		check = Math.sqrt(Math.pow((odometer.getX() - x), 2) + Math.pow((odometer.getY() - y), 2));

		if (repeat && check >= 1) {
			travelTo(x, y, immediateRet);
		}

	}

	/**
	 * TurnTo function which takes an angle and boolean as arguments The boolean
	 * controls whether or not to stop the motors when the turn is completed
	 * 
	 * @param turnAngle
	 *            The <code>double</code> angle that is required to be turned
	 * @param stop
	 *            The <code>boolean</code> decision to decide whether the method
	 *            will wait when turning
	 */
	public void turnTo(double turnAngle, boolean stop) {

		isTurning = true;

		// Get the angle the robot needs to turn
		double angleNeedToTravel = (turnAngle - odometer.getAng()) % 360;

		// Get the minimal angle
		if ((angleNeedToTravel > 180) || (angleNeedToTravel < -180)) {
			if (angleNeedToTravel > 180) {
				angleNeedToTravel = angleNeedToTravel - 360;
			} else {
				angleNeedToTravel = angleNeedToTravel + 360;
			}
		}

		// if the angle is not within the given error margin
		if (Math.abs(angleNeedToTravel) > DEG_ERR) {

			// if angle change is positive we move clockwise
			if (angleNeedToTravel > 0) {

				Motor.A.setSpeed(SLOW);
				Motor.B.setSpeed(SLOW);

				Motor.A.rotate(
						convertAngle(wheelRadius, width,
								Math.abs(angleNeedToTravel)), true);
				Motor.B.rotate(
						-convertAngle(wheelRadius, width,
								Math.abs(angleNeedToTravel)), false);

				// if angle is negative move counterclockwise
			} else {

				Motor.A.setSpeed(SLOW);
				Motor.B.setSpeed(SLOW);

				Motor.A.rotate(
						-convertAngle(wheelRadius, width,
								Math.abs(angleNeedToTravel)), true);
				Motor.B.rotate(
						convertAngle(wheelRadius, width,
								Math.abs(angleNeedToTravel)), false);
			}
		}

		// Stop the motors after we turned the amount we desired
		Motor.B.stop();
		Motor.A.stop();
		isTurning = false;
	}

	/**
	 * This method makes to robot go foward a set travelDis in cm
	 * 
	 * @param travelDis
	 *            The <code>double</code> travel distance that the robot will
	 *            travel
	 */
	public void goForward(double travelDis) {
		this.travelTo(Math.cos(Math.toRadians(this.odometer.getAng()))
				* travelDis, Math.cos(Math.toRadians(this.odometer.getAng()))
				* travelDis, false);

	}

	/**
	 * This method makes to robot go forward slowly a set travelDis in cm
	 * 
	 * @param speed
	 *            The <code> int </code> speed that the robot will travel at
	 */
	public void goForwardSpeed(int speed) {
		this.setSpeeds(speed, speed);
	}

	/**
	 * This method returns the <code>boolean</code> value that keeps track
	 * whether the robot is currently turning.
	 * 
	 * @return isTurning the <code>boolean</code> value determining whether the
	 *         robot is turning.
	 */
	public boolean isTurning() {
		return isTurning;
	}

	/**
	 * This method make the robot move forward without navigation or block
	 * avoidance. Used only for localization.
	 * @param speed The speed of the wheels.Unit is degree per second
	 * @param travelDis The distance need to travel in <code>double</code>. Unit is centimeter.
	 */
	public void moveForward(int speed,double travelDis) {
		Motor.A.setSpeed(speed);
		Motor.B.setSpeed(speed);
		Motor.A.rotate(convertDistance(wheelRadius, travelDis), true);
		Motor.B.rotate(convertDistance(wheelRadius, travelDis), false);
	}
	/** This method stops both motors of the robot.
	 *
	 */
	public void stopMotors(){
		Motor.A.stop();
		Motor.B.stop();
	}
}