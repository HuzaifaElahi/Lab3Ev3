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

public class Navigation {

	// Parameters: adjust these for desired performance

	private static final int bandCenter = 30; // Offset from the wall (cm)
	private static final int bandWidth = 3; // Width of dead band (cm)
	private static final int motorLow = 100; // Speed of slower rotating wheel (deg/sec)
	private static final int motorHigh = 200; // Speed of the faster rotating wheel (deg/seec)
	private static final TextLCD lcd = LocalEV3.get().getTextLCD();
	public static final double WHEEL_RAD = 2.2;
	public static final double TRACK = 13.72;
	private static double[] path;

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

	public static void main(String[] args) throws Exception {

		int option = 0;
		while (option == 0) // and wait for a button press. The button
			option = Button.waitForAnyPress(); // ID (option) determines what type of control to use
		switch (option) {
		case Button.ID_LEFT:
			// run 1
			double []pathBuff1 = {60, 30,30, 30,30, 60,60, 0};
			int i = 0;
			for(double d : pathBuff1) {
				path[i] = d;
				i++;
			}
			break;
		case Button.ID_RIGHT:
			// run 2
			double []pathBuff2 = {0, 60,60, 0};
			int j = 0;
			for(double d : pathBuff2) {
				path[j] = d;
				j++;
			}
			break;
		default:
		}

		@SuppressWarnings("resource") // Because we don't bother to close this resource
		SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
		SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provides samples from this instance
		float[] usData = new float[usDistance.sampleSize()]; // usData is the buffer in which data are returned
		UltrasonicPoller usPoller = null; // the selected controller on each cycle
		usPoller = new UltrasonicPoller(usDistance, usData);
		// Start the poller thread
		usPoller.start();

		Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
		OdometryCorrection odometryCorrection = new OdometryCorrection();

		// Start odometer threads
		Thread odoThread = new Thread(odometer);
		odoThread.start();
		Thread odoCorrectionThread = new Thread(odometryCorrection);
		odoCorrectionThread.start();
		for(int index = 0 ; index < path.length ; index++ ) {
			travelTo(path[index], path[index+1]);
		}

		// Wait here forever until button pressed to terminate
		Button.waitForAnyPress();
		System.exit(0);

	}

	// TODO This method causes the robot to travel to the absolute field location (x,  y),
	// specified  in tilepoints.This method should continuously call turnTo(double theta)
	// and then set the motor speed to forward(straight). This makes sure heading is updated
	// till you reach your destination. This method will poll the odometer for information
	void travelTo(double x, double y) {

	}

	// TODO This method causes the robot to turn (on point) to the absolute heading theta.
	// This method should turn a MINIMAL angle to its target
	void turnTo(double theta) {

	}

	// TODO this method returns true if another thread has called travelTo() or turnTo()
	// and the method has yet to return; false otherwise.
	boolean isNavigating() {
		return false;

	}

}
