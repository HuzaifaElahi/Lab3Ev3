package ca.mcgill.ecse211.lab3;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Navigation extends Thread {

	// Parameters: adjust these for desired performance

	private static final int BAND_CENTER = 30; // Offset from the wall (cm)
	private static final int BANDWIDTH = 3; // Width of dead band (cm)
	private static final int MOTOR_LOW = 100; // Speed of slower rotating wheel (deg/sec)
	private static final int MOTOR_HIGH = 200; // Speed of the faster rotating wheel (deg/seec)
	private static final int ROTATE_SPEED = 150;
	private static final TextLCD lcd = LocalEV3.get().getTextLCD();
	public static final double WHEEL_RAD = 2.2;
	public static final double TRACK = 13.72;
	double[] path;
	private static Navigation nav;

	//Motors and distance sensor
	private static final Port usPort = LocalEV3.get().getPort("S1");
	public static final EV3LargeRegulatedMotor leftMotor =
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	public static final EV3LargeRegulatedMotor rightMotor =
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));

	//Color Sensor and its variables are initialized
	private static final Port portColor = LocalEV3.get().getPort("S2");
	public static SensorModes myColor = new EV3ColorSensor(portColor);
	public static SampleProvider myColorSample = myColor.getMode("Red");
	static float[] sampleColor = new float[myColor.sampleSize()]; 
	
	// Variables for odometer
	Odometer odometer = null;
	OdometryCorrection odometryCorrection = null;
	
	
	   public void run() {
	      
	     
      try {
        odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
	     
      try {
        odometryCorrection = new OdometryCorrection();
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
	        
	     Thread odoThread = new Thread(odometer);
	     odoThread.start();
	     Thread odoCorrectionThread = new Thread(odometryCorrection);
	     odoCorrectionThread.start();
	     
	     @SuppressWarnings("resource") // Because we don't bother to close this resource
	     SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
	     SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provides samples from this instance
	     float[] usData = new float[usDistance.sampleSize()]; // usData is the buffer in which data are returned
	     UltrasonicPoller usPoller = null; // the selected controller on each cycle
	     usPoller = new UltrasonicPoller(usDistance, usData);
	     // Start the poller thread
	     usPoller.start();
	       
	     
	     for(int index = 0 ; index < nav.path.length -1 ; index += 2 ) {
           travelTo(nav.path[index], nav.path[index+1]);
	     } 
	    }
	   
	public static void main(String[] args) throws Exception {
		nav = new Navigation();
		int option = 0;
		while (option == 0) // and wait for a button press. The button
			option = Button.waitForAnyPress(); // ID (option) determines what type of control to use
		switch (option) {
		case Button.ID_LEFT:
			// run 1
			double []pathBuff1 = {60, 30,30, 30,30, 60,60, 0};
			int i = 0;
			for(double d : pathBuff1) {
				nav.path[i] = d;
				i++;
			}
			break;
		case Button.ID_RIGHT:
			// run 2
			double []pathBuff2 = {0, 60,60, 0};
			int j = 0;
			for(double d : pathBuff2) {
				nav.path[j] = d;
				j++;
			}
			break;
		default:
		}

		

		
		// Start odometer threads
		


		// Wait here forever until button pressed to terminate
		Button.waitForAnyPress();
		System.exit(0);

	}

	// TODO This method causes the robot to travel to the absolute field location (x,  y),
	// specified  in tilepoints.This method should continuously call turnTo(double theta)
	// and then set the motor speed to forward(straight). This makes sure heading is updated
	// till you reach your destination. This method will poll the odometer for information
	void travelTo(double x, double y) {
	  double[] result = odometer.getXYT();
	  double odoX = result[0];
	  double odoY = result[1];
      double odoTheta = result[2];
	  double difX = x - odoX;
	  double difY = y - odoY;
	  
	  turnTo(Math.atan2(difY, difX));
	  
	  leftMotor.forward();
      rightMotor.forward();
      
      leftMotor.setSpeed(MOTOR_HIGH);
      rightMotor.setSpeed(MOTOR_HIGH);
      leftMotor.forward();
      rightMotor.forward();
      
	  /*
	  leftMotor.setSpeed(FORWARD_SPEED-1); //Weaker motor compensation
      rightMotor.setSpeed(FORWARD_SPEED);
      leftMotor.rotate(convertDistance(leftRadius, 3 * TILE_SIZE), true);
      rightMotor.rotate(convertDistance(rightRadius, 3 * TILE_SIZE), false);
      */
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
      
      // Turns clockwise
      if (newTheta < 0) {
        leftMotor.rotate(convertAngle(WHEEL_RAD, TRACK, thetaDegrees), true);
        rightMotor.rotate(-convertAngle(WHEEL_RAD, TRACK, thetaDegrees), false);
      }
      
      // Turns anti-clockwise
      if (newTheta > 0) {
        leftMotor.rotate(-convertAngle(WHEEL_RAD, TRACK, thetaDegrees), true);
        rightMotor.rotate(convertAngle(WHEEL_RAD, TRACK, thetaDegrees), false); 
      }
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
	

	// TODO this method returns true if another thread has called travelTo() or turnTo()
	// and the method has yet to return; false otherwise.
	boolean isNavigating() {
		return false;

	}

}