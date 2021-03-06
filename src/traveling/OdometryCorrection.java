package traveling;

import lejos.nxt.*;
import lejos.util.Delay;

/**
 * The <code>OdometryCorrection</code> class corrects the odometry whenever one of the two 
 * color sensors of the robot crosses a grid line. 
 * 
 * It has a private method which determines which grid line is closest to a given coordinate.
 * 
 * An instance of this class holds an instance of the <code>Navigation</code> class which will be
 * interacting with it, as well as an instance of <code>Odometry</code> which it updates when
 * a grid line is crossed.
 * 
 * @see Odometer
 * @see Navigation
 * 
 * @author Stefan T
 *
 */

public class OdometryCorrection extends Thread {
	private static final long CORRECTION_PERIOD = 10;
	private Odometer odometer;
	private Navigation navigation;
	private ColorSensor leftCS;
	private ColorSensor rightCS;

	//variables
	private final double LEFT_LIGHT_THRESHOLD = 0.85, RIGHT_LIGHT_THRESHOLD = 0.87;
	private final static double X_OFFSET = 7.3;
	private final static double Y_OFFSET = 7.3;

	// constructor

	/**
	 * The constructor of the <code>OdometryCorrection</code> initializes instances of the class <code>Odometer</code>.
	 * It initializes the <code>Navigation</code> as the navigation that is used by the odometer.
	 * 
	 * @param odometer 	The <code>Odometer</code> that is used for the robot
	 * @param leftCS 	The left <code>ColorSensor</code> that is used to check grid lines
	 * @param rightCS 	The right <code>ColorSensor</code>that is used to check grid lines
	 */
	public OdometryCorrection(Odometer odometer, ColorSensor leftCS, ColorSensor rightCS) {		
		this.odometer = odometer;
		this.leftCS = leftCS;
		this.rightCS = rightCS;
		this.navigation = odometer.getNavigation();
	}

	// run method (required for Thread)

	/** If the <code>boolean isTurning</code> from the <code>Navigation</code> is false,
	 * check if either of the two <code>ColorSensors</code> crosses a grid line. If one does, 
	 * determine which grid line is closest and update the <code>Odometer</code> accordingly.
	 * 
	 * {@inheritDoc}
	 */
	public void run() {
		// Variables
		int ambientLeft = 0, ambientRight = 0;
		double tempAngle = 0, XError, YError;
		long correctionStart, correctionEnd;

		// 	Calculate the hypotenuse, as well as the angle offset for both the left and right
		//	ColorSensors.
		double hypotenuse = Math.sqrt(X_OFFSET*X_OFFSET + Y_OFFSET*Y_OFFSET);
		double leftOffset = Math.PI + Math.atan(Y_OFFSET/X_OFFSET);
		double rightOffset = Math.PI - Math.atan(Y_OFFSET/X_OFFSET);

		leftCS.setFloodlight(true);
		rightCS.setFloodlight(true);

		//	Calculate the average value of getRawLightValue, this is the value of the ambient light.
		for(int i = 0; i < 20; i++)
		{
			ambientLeft += leftCS.getRawLightValue();
			ambientRight += rightCS.getRawLightValue();
			Delay.msDelay(10);	
		}

		ambientLeft /= 20;
		ambientRight /= 20;

		//	This while loop is used to check if either of the ColorSensors crosses a grid line.
		// 	If one does, it updates the odometer. It only does this when the robot is not turning.

		while (true) {

			correctionStart = System.currentTimeMillis();

			//	The odometry correction only runs if the robot is not turning.
			if(!navigation.isTurning()){
				//	If the light value read by the ColorSensor is below the ambient light
				//	by a percentage, the ColorSensor has crossed a grid line.
				if (leftCS.getRawLightValue() < ambientLeft * LEFT_LIGHT_THRESHOLD) {
					//Sound.beep();

					//	The temporary angle is the angle that the robot is currently at plus
					//	the left offset angle.
					tempAngle = odometer.getAng()*Math.PI/180 + leftOffset;

					//	If the angle is over 2*PI, it is corrected.
					if (tempAngle > 2*Math.PI)
						tempAngle -= 2*Math.PI;

					//	The XError and YError are set as the difference between the closest grid 
					//	line according to the odometer and the measured position of the grid line.
					XError = Math.abs(getLine(odometer.getX() + hypotenuse * Math.cos(tempAngle)) - (odometer.getX() + hypotenuse * Math.cos(tempAngle)));
					YError = Math.abs(getLine(odometer.getY() + hypotenuse * Math.sin(tempAngle)) - (odometer.getY() + hypotenuse * Math.sin(tempAngle)));

					//	The minimum between the X and Y error is found. If the X error is the 
					//	smaller, the X position of the odometer is updated. If the Y is smaller,
					//	the Y position of the odometer is updated.
					if(Math.min(XError,  YError) == XError)				
						odometer.setX(getLine(odometer.getX() + hypotenuse * Math.cos(tempAngle)) - hypotenuse * Math.cos(tempAngle));					
					else
						odometer.setY(getLine(odometer.getY() + hypotenuse * Math.sin(tempAngle)) - hypotenuse * Math.sin(tempAngle));					
				}

				//	The following if statement is nearly identical to the one above. The right 
				//	ColorSensor is polled instead of the left one.
				if (rightCS.getRawLightValue() < ambientRight * RIGHT_LIGHT_THRESHOLD) {
					//Sound.twoBeeps();

					//	The rightOffset is used to calculate the tempAngle instead of the leftOffset.
					tempAngle = odometer.getAng()*Math.PI/180 + rightOffset;

					if (tempAngle > 2*Math.PI)
						tempAngle -= 2*Math.PI;

					XError = Math.abs(getLine(odometer.getX() + hypotenuse * Math.cos(tempAngle)) - (odometer.getX() + hypotenuse * Math.cos(tempAngle)));
					YError = Math.abs(getLine(odometer.getY() + hypotenuse * Math.sin(tempAngle)) - (odometer.getY() + hypotenuse * Math.sin(tempAngle)));

					if(Math.min(XError,  YError) == XError)				
						odometer.setX(getLine(odometer.getX() + hypotenuse * Math.cos(tempAngle)) - hypotenuse * Math.cos(tempAngle));					
					else
						odometer.setY(getLine(odometer.getY() + hypotenuse * Math.sin(tempAngle)) - hypotenuse * Math.sin(tempAngle));					


				}

			}
			// this ensures the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
		}
	}





	// depending on the heading of the robot find the closest grid line it just crossed.
	private static double getLine(double coordinate) {
		return Math.round(coordinate / 30.3) * 30.3;
	}



}