package maincontrol;
import traveling.Odometer;
import lejos.nxt.LCD;
import lejos.util.Timer;
import lejos.util.TimerListener;
/**
 * The <code>LCDInfo</code> class is used to print stuff to the LCD monitor on the NXT brick.
 * 
 * An instance of the <code>LCDInfo</code> class holds a <code>int</code> refresh rate, and
 * <code>Timer</code> lcdTimer which controls the rate at which elements will be displayed on
 * the screen.
 * 
 * @author  Alessandro Parisi
 * @version 1.0
 * @since   1.0
 *
 */
public class LCDInfo implements TimerListener{
    	private static final int LCD_REFRESH = 350;
	private Timer lcdTimer;
	
	/**
	 * The constructor of the <code>LCDInfo</code> class will initiate the <code>Odometer</code>,
	 * and <code>Timer</code>. It then starts the <code>Timer</code>
	 * @param odo
	 */
	public LCDInfo(Odometer odo) {
		this.lcdTimer = new Timer(LCD_REFRESH, this);
		LCD.clear();
		
		// start the timer
		lcdTimer.start();
	}
	
	/**
	 * This method is used to time out the timer.
	 * {@inheritDoc}
	 */
	public void timedOut() { 
		
	}
}