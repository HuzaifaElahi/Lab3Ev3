package ca.mcgill.ecse211.lab3;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;


public class Controller {
	/*
	 * 
	 * Map 1: (0,2), (1,1), (2,2), (2,1), (1,0)
	 * Map 2: (1,1), (0,2), (2,2), (2,1), (1,0)
	 * Map 3: (1,0), (2,1), (2,2), (0,2), (1,1)
	 * Map 4: (0,1), (1,2), (1,0), (2,1), (2,2)
	 */
	private static final TextLCD lcd = LocalEV3.get().getTextLCD();
	static double[] path1 = {0, 2,     1, 1,     2, 2,     2, 1,   1, 0};
	static double[] path2 = {1, 1,     0, 2,     2, 2,     2, 1,   1, 0};
	static double[] path3 = {1, 0,     2, 1,     0, 2,     0, 2,   1, 1};
	static double[] path4 = {0, 1,     1, 2,     1, 0,     2, 1,   2, 2};
	static double[] finalPath;

	public static void main(String[] args) throws Exception {
		
		int buttonChoice;

		
		do {
		      // clear the display
		      lcd.clear();

		      // ask the user whether the motors should drive in a square or float
		      lcd.drawString("<Map 1 | Map 3>", 0, 0);
		      lcd.drawString("       |       ", 0, 1);
		      lcd.drawString(" Map 2 | Map 4 ", 0, 2);
		      lcd.drawString("       |       ", 0, 3);
		      lcd.drawString("       |       ", 0, 4);

		      buttonChoice = Button.waitForAnyPress(); // Record choice (left or right press)
		    } while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);

		    if (buttonChoice == Button.ID_LEFT) {
		      // Float the motors
		    	 lcd.drawString("<      |      >", 0, 0);
			     lcd.drawString("       |       ", 0, 1);
			     lcd.drawString(" Map 1 | Map 2 ", 0, 2);
			     lcd.drawString("       |       ", 0, 3);
			     lcd.drawString("       |       ", 0, 4);
			     buttonChoice = Button.waitForAnyPress(); // Record choice (left or right press)
			     if (buttonChoice == Button.ID_RIGHT) {
			    	 finalPath = path2;
			      }
			     else {
			    	 finalPath = path1;
			     }

		    } else {
		      // clear the display
		      lcd.clear();

		      // ask the user whether odometery correction should be run or not
		      	 lcd.drawString("<      |      >", 0, 0);
			     lcd.drawString("       |       ", 0, 1);
			     lcd.drawString(" Map 3 | Map 4 ", 0, 2);
			     lcd.drawString("       |       ", 0, 3);
			     lcd.drawString("       |       ", 0, 4);

		      buttonChoice = Button.waitForAnyPress(); // Record choice (left or right press)
		      
		      if (buttonChoice == Button.ID_RIGHT) {
			    	 finalPath = path4;
			      }
			     else {
			    	 finalPath = path3;
			     }
		    }
		
		
		Navigation nav = new Navigation(finalPath);
		nav.start();

		while (Button.waitForAnyPress() != Button.ID_ESCAPE)
			;
		System.exit(0);



	}

}