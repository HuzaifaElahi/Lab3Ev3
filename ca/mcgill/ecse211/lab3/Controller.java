package ca.mcgill.ecse211.lab3;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Controller {

	public static void drive(Navigation nav, EV3LargeRegulatedMotor leftmotor, EV3LargeRegulatedMotor rightmotor, double wheelRad,
			double wheelRad2, double track) {
		// TODO Auto-generated method stub
	    
		for(int index = 0 ; index < nav.path.length ; index++ ) {
			nav.travelTo(nav.path[index], nav.path[index+1]);
		}
		
	}
	
	

}
