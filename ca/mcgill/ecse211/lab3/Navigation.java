package ca.mcgill.ecse211.lab3;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;


public class Navigation extends Thread implements Runnable {

  // Parameters: adjust these for desired performance

  private static final int MOTOR_HIGH = 200; // Speed of the faster rotating wheel (deg/seec)
  private static final int ROTATE_SPEED = 150;
  private static final TextLCD lcd = LocalEV3.get().getTextLCD();
  public static final double WHEEL_RAD = 2.2;
  public static final double SQUARE_SIZE = 30.48;
  public static final double TRACK = 13.72;
  double[] path;
  

  
  //Motors and distance sensor
  private static final Port usPort = LocalEV3.get().getPort("S1");
  public static final EV3LargeRegulatedMotor leftMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
  public static final EV3LargeRegulatedMotor rightMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));

  public Navigation(double ... path) {
	  this.path = path;
  }

  
  // Variables for odometer
  Odometer odometer = null;

  public void run() {

    try {
      odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    try {
      //odometryCorrection = new OdometryCorrection();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    Thread odoThread = new Thread(odometer);
    odoThread.start();
    //Thread odoCorrectionThread = new Thread(odometryCorrection);
    //odoCorrectionThread.start();
    odometer.setXYT(0, 0, 0);

    for(int index = 0 ; index < path.length - 1;) {
    	System.out.print("FUCK DPM");
      travelTo(path[index++], path[index++]);
      } 
  }
  
  private static double prevAngle = 0;

	public void travelTo(double x, double y) {
		// Define variables
		double odometer[] = { 0, 0, 0 }, absAngle = 0, dist = 0, deltaX = 0, deltaY = 0;
		
		// Set navigating to true
		navigating = true;

		// Get odometer readings
		try {
			odometer = Odometer.getOdometer().getXYT();
		} catch (Exception e) {
			// Do nothing lol
			e.printStackTrace();
		}

		// Convert X & Y coordinates to actual length (cm)
		x = x*SQUARE_SIZE;
		y = y*SQUARE_SIZE;

		// Set odometer reading angle as prev angle as well
		prevAngle = odometer[2];

		// Get displacement to travel on X and Y axis
		deltaX = x - odometer[0];
		deltaY = y - odometer[1];
		
		// Displacement to point (hypothenuse)
		dist = Math.hypot(Math.abs(deltaX), Math.abs(deltaY));

		// Get absolute angle the robot must be facing
		absAngle = Math.toDegrees(Math.atan2(deltaX, deltaY));

		// If the value of absolute angle is negative, loop it back
		if (absAngle < 0)
			absAngle = 360 - Math.abs(absAngle);

		// Make robot turn to the absolute angle
		turnTo(absAngle);

		// Set robot speed
		leftMotor.setSpeed(MOTOR_HIGH);
		rightMotor.setSpeed(MOTOR_HIGH);

		
		// Move distance to the waypoint after robot has adjusted angle
		leftMotor.rotate(convertDistance(WHEEL_RAD, dist), true);
		rightMotor.rotate(convertDistance(WHEEL_RAD, dist), false);
		
		System.out.println(path[0] + "" + path[1]);
		System.out.println(odometer[0] + "" + odometer[1]);

	}
	/**
	 * This method causes the robot to turn (on point) to the absolute heading theta
	 */
	public static void turnTo(double theta) {
		boolean turnLeft = false;
		double deltaAngle = 0;
		// Get change in angle we want
		deltaAngle = theta - prevAngle;

		// If deltaAngle is negative, loop it back
		if (deltaAngle < 0) {
			deltaAngle = 360 - Math.abs(deltaAngle);
		}

		// Check if we want to move left or right
		if (deltaAngle > 180) {
			turnLeft = true;
			deltaAngle = 360 - Math.abs(deltaAngle);
		} else {
			turnLeft = false;
		}

		// Set slower rotate speed
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		// Turn motors according to which direction we want to turn in
		if (turnLeft) {
			leftMotor.rotate(-convertAngle(WHEEL_RAD, TRACK, deltaAngle), true);
			rightMotor.rotate(convertAngle(WHEEL_RAD, TRACK, deltaAngle), false);
		} else {
			leftMotor.rotate(convertAngle(WHEEL_RAD, TRACK, deltaAngle), true);
			rightMotor.rotate(-convertAngle(WHEEL_RAD, TRACK, deltaAngle), false);
		}

	}
	
	static boolean navigating = false;

	public static boolean isNavigating() {
		return navigating;
	}

  /**
   * This method is a helper that allows the conversion of a distance to the total rotation of each wheel need to
   * cover that distance.
   * 
   * @param radius
   * @param distance
   * @return int
   */
  private static int convertDistance(double radius, double distance) {
    return (int) ((180.0 * distance) / (Math.PI * radius));
  }

  /**
   * This method allows the conversion of an angle and it's calculated distance to the total rotation of each wheel need to
   * cover that distance.
   * 
   * @param radius
   * @param distance
   * @return int
   */
  private static int convertAngle(double radius, double width, double angle) {
    return convertDistance(radius, Math.PI * width * angle / 360.0);
  }

}
