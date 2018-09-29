package ca.mcgill.ecse211.lab3;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Controller {
	  
  public static void main(String[] args) throws Exception {
    Navigation nav = new Navigation();
   
   /* int option = 0;
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
    Button.waitForAnyPress();*/

    nav.start();
  
    while (Button.waitForAnyPress() != Button.ID_ESCAPE)
		;
	System.exit(0);
      
    
    
  }
	
	

}