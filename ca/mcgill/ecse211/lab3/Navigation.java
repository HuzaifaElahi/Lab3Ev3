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
  double[] path= {0, 60,     30, 30,     60, 60,     60, 0};

  
  //Motors and distance sensor
  private static final Port usPort = LocalEV3.get().getPort("S1");
  public static final EV3LargeRegulatedMotor leftMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
  public static final EV3LargeRegulatedMotor rightMotor =
      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));


  
  // Variables for odometer
  Odometer odometer = null;
  //OdometryCorrection odometryCorrection = null;


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

    for(int index = 0 ; index < path.length - 3; index += 2 ) {
    	System.out.print("FUCK DPM");
      travelTo(path[index], path[index+1]);
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

/*
  // TODO This method causes the robot to travel to the absolute field location (x,  y),
  // specified  in tilepoints.This method should continuously call turnTo(double theta)
  // and then set the motor speed to forward(straight). This makes sure heading is updated
  // till you reach your destination. This method will poll the odometer for information
  void travelTo(double x, double y) {
    navigating = true;
    double[] result = odometer.getXYT();
    double odoX = result[0];
    double odoY = result[1];
    double odoTheta = result[2];
    double difX = x - odoX;
    double difY = y - odoY;

    turnTo(Math.atan2(difY, difX));

    leftMotor.forward();
    rightMotor.forward();

    while (navigating) {
      leftMotor.setSpeed(MOTOR_HIGH);
      rightMotor.setSpeed(MOTOR_HIGH);
      leftMotor.forward();
      rightMotor.forward();

      result = odometer.getXYT();
      odoX = result[0];
      odoY = result[1];

      difX = x - odoX;
      difY = y - odoY;
      
      if (Math.pow(difX,2) + Math.pow(difY, 2) < 0.5) {
        leftMotor.stop(true);
        rightMotor.stop(false);
        navigating = false;
        return;
      }



    }    

  }


  // TODO This method causes the robot to turn (on point) to the absolute heading theta.
  // This method should turn a MINIMAL angle to its target
  void turnTo(double theta) {

    double[] result = odometer.getXYT();
    double headingAngle = result[2];

    // Convert theta to degrees
    double thetaDegrees = Math.toDegrees(theta);

    double newTheta = thetaDegrees - headingAngle;

    // Motor speed for rotation
    leftMotor.setSpeed(ROTATE_SPEED);
    rightMotor.setSpeed(ROTATE_SPEED);
    
    leftMotor.setSpeed(MOTOR_HIGH);
    rightMotor.setSpeed(MOTOR_HIGH);
    leftMotor.forward();
    rightMotor.forward();


    // Turns clockwise
    if (newTheta <= 0) {
      leftMotor.rotate(convertAngle(WHEEL_RAD, TRACK, thetaDegrees), true);
      rightMotor.rotate(-convertAngle(WHEEL_RAD, TRACK, thetaDegrees), false);
    }

    // Turns anti-clockwise
    if (newTheta > 0) {
      leftMotor.rotate(-convertAngle(WHEEL_RAD, TRACK, thetaDegrees), true);
      rightMotor.rotate(convertAngle(WHEEL_RAD, TRACK, thetaDegrees), false); 
    }
  } */

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

/*
  // TODO this method returns true if another thread has called travelTo() or turnTo()
  // and the method has yet to return; false otherwise.
  boolean isNavigating() {
    return false;

  }
*/
}
