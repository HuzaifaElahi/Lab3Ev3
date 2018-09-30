package ca.mcgill.ecse211.lab3;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

/**
 * This is the Navigation class which extends Thread and implements
 * Runnable and Ultrasonic Controller. It uses the Ultrasonic sensor
 * to handle cases where an object is detected in the path of the robot 
 * and otherwise travels to the selected waypoints that are to it by
 * Controller in the NavWithObstacle constructor
 * 
 * @author Huzaifa, Jake
 * 
 */
public class NavWithObstacle extends Thread implements Runnable, UltrasonicController {

	
	// Parameters: Can adjust these for desired performance
	private static final int MOTOR_HIGH = 200;      // Speed of the faster rotating wheel (deg/seec)
	private static final int MOTOR_LOW = 100;       // Speed of the faster rotating wheel (deg/seec)
	private static final int ROTATE_SPEED = 150;    // Speed upon rotation
	private boolean isDanger = false;               // If object detected

	private static final TextLCD lcd = LocalEV3.get().getTextLCD();     // Lcd screen
	public static final double WHEEL_RAD = 2.2;                         // Radius of the wheel
	public static final double SQUARE_SIZE = 30.48;                     // Size of tiles
	public static final double TRACK = 13.72;                           // Distance from wheel to wheel
	int[] path;
	SensorModes usSensor = new EV3UltrasonicSensor(usPort);                      // usSensor is the instance
	SampleProvider usDistance = usSensor.getMode("Distance");                    // usDistance provides samples 
	float[] usData = new float[usDistance.sampleSize()];                         // usData is the buffer for data
	UltrasonicPoller usPoller = new UltrasonicPoller(usDistance, usData, this);  // Instantiate poller

	//Motors and distance sensor initialized
	private static final Port usPort = LocalEV3.get().getPort("S1");
	public static final EV3LargeRegulatedMotor leftMotor =
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	public static final EV3LargeRegulatedMotor rightMotor =
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));

	// Variables for odometer
	Odometer odometer = null;
	private int distance;
	private static double prevAngle = 0;
	static boolean navigating = false;


	/**
	 * Contructor, takes in and sets path passed by user
	 * selection in Controller class
	 * 
	 * @param finalPath
	 */
	public NavWithObstacle(int ... finalPath) {
		this.path = finalPath;
	}


	/** 
	 * Run method, runs when thread is initialized
	 * 
	 * @return void
	 */
	public void run() {
		usPoller.start();     // Start poller thread

		//Fetch odometer
		try {
			odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Thread odoThread = new Thread(odometer);    // Start odometer thread
		odoThread.start();
		odometer.setXYT(0, 0, 0);    // Initialize odometer

		// Call travel to on every waypoint
		for(int index = 0 ; index < path.length - 1;) {
			travelTo(path[index++], path[index++]);
		} 
	}

	/**
	 * This method makes robot move in the direction of the
	 * waypoint whose coordinates are passed as arguments
	 * 
	 * @param x
	 * @param y
	 * @return void
	 */
	public void travelTo(double x, double y) {
		// Define variables
		double odometer[] = { 0, 0, 0 }, absAngle = 0, dist = 0, deltaX = 0, deltaY = 0;

		// Set navigating to true
		navigating = true;

		// Get odometer readings
		try {
			odometer = Odometer.getOdometer().getXYT();
		} catch (Exception e) {
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

	}
	
	/**
	 * This method causes the robot to turn (on point) to the absolute heading theta
	 * 
	 * @param theta
	 * @return void
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

	/**
	 * This method returns the static boolean, navigating
	 * 
	 * @return boolean
	 */
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

	/** 
	 * This method overrides the method in Ultrasonic Controller and 
	 * updates the inDanger boolean if an object is detected
	 * within 10 cm of the sensor
	 * 
	 * @param distance
	 * @return void
	 * 
	 */
	@Override
	public void processUSData(int distance) {
		this.distance = distance;
		if(distance < 10) {
			isDanger = true;
		} 		
	}

	/** 
	 * This method overrides the method in Ultrasonic Controller
	 * and returns us the distance of the object detected by the sensor
	 * 
	 * @return int
	 */
	@Override
	public int readUSDistance() {
		return this.distance;
	}
}
