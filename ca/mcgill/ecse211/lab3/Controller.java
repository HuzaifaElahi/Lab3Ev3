package ca.mcgill.ecse211.lab3;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;


/**
 * @author Huzaifa 260726386, Jake
 * This the the main class where we 
 * 1. Start the program,
 * 2. Start the UI interface for map selection
 * 3. Call the Navigation class and begin threads
 *
 */
public class Controller {
	/*
	 * Initialize 4 different maps:
	 * Map 1: (0,2), (1,1), (2,2), (2,1), (1,0)
	 * Map 2: (1,1), (0,2), (2,2), (2,1), (1,0)
	 * Map 3: (1,0), (2,1), (2,2), (0,2), (1,1)
	 * Map 4: (0,1), (1,2), (1,0), (2,1), (2,2)
	 */
	static int[] path1 = {0, 2,     1, 1,     2, 2,     2, 1,   1, 0};
	static int[] path2 = {1, 1,     0, 2,     2, 2,     2, 1,   1, 0};
	static int[] path3 = {1, 0,     2, 1,     0, 2,     0, 2,   1, 1};
	static int[] path4 = {0, 1,     1, 2,     1, 0,     2, 1,   2, 2};
	
	// Initialize selected path and LCD screen
	private static final TextLCD lcd = LocalEV3.get().getTextLCD();
	static int[] finalPath;
	
	/**
	 * This is the main method for this class where the program starts
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		int buttonChoice;   		// Store selected button


		
		do {
		      lcd.clear();   		// clear the display

		      // Ask the user whether map 1 or 2 / map 3 or 4 should be selected
		      lcd.drawString("<Map 1 | Map 3>", 0, 0);
		      lcd.drawString("       |       ", 0, 1);
		      lcd.drawString(" Map 2 | Map 4 ", 0, 2);
		      lcd.drawString("       |       ", 0, 3);
		      lcd.drawString("       |       ", 0, 4);

		      buttonChoice = Button.waitForAnyPress();      // Record choice (left or right press)
		   
		      // Until button pressed
		    } while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT); 

		    if (buttonChoice == Button.ID_LEFT) {
		      // Pick between map 1 or map 2
		    	 lcd.drawString("<      |      >", 0, 0);
			     lcd.drawString("       |       ", 0, 1);
			     lcd.drawString(" Map 1 | Map 2 ", 0, 2);
			     lcd.drawString("       |       ", 0, 3);
			     lcd.drawString("       |       ", 0, 4);
			     buttonChoice = Button.waitForAnyPress();   // Record choice (left or right press)
			     
			     if (buttonChoice == Button.ID_RIGHT) {     
			    	 finalPath = path2;      // Set map 2
			      }
			     else {
			    	 finalPath = path1;      // Set map 1        
			     }

		    } else {
		    
		      lcd.clear();      // clear the display

		      // Pick between map 3 or map 4
		      	 lcd.drawString("<      |      >", 0, 0);
			     lcd.drawString("       |       ", 0, 1);
			     lcd.drawString(" Map 3 | Map 4 ", 0, 2);
			     lcd.drawString("       |       ", 0, 3);
			     lcd.drawString("       |       ", 0, 4);

		      buttonChoice = Button.waitForAnyPress();   // Record choice (left or right press)
		      
		      if (buttonChoice == Button.ID_RIGHT) {
			    	 finalPath = path4;      // Set map 4
			      }
			     else {
			    	 finalPath = path3;      // Set map 3
			     }
		    }
		
		// Start NavWithObstacle thread
		NavWithObstacle nav = new NavWithObstacle(finalPath);
		nav.start();

		// If button is pressed, exit the program
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);



	}

}