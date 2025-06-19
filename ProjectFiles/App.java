package com.kibler.capstone.course_prerequisite_manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.kibler.capstone.course_prerequisite_manager.database.CourseDatabaseManager;

/*					 Programmer: Michael Kibler
 * 	
 * 					Course Pre-Requisiste Manager:
 * 
 * 		This application is aimed at providing students with a way to easily
 * 	display the required prerequisites to any specific class they plan on 
 * 	taking. Start the application and read the menu for instructions.
 * 
 * 
 */

public class App {

	private static CourseDatabaseManager dbManager = new CourseDatabaseManager();
	private static CourseGraph graph = new CourseGraph();
	
    public static void main(String[] args) {
    	
    	// Connect to the database and load the data into CourseGraph
    	dbManager.openDB();
    	dbManager.onCreate();
    	
    	
    	//causing error. Need to ensure dbmanager has tables before loading.
    	graph.loadGraph(dbManager);
     
      	// Display Application UI/Menu
      	displayMenu();
        
        // Create a scanner to capture user input
        Scanner scanner = new Scanner(System.in);		
        String userInput = scanner.nextLine();
        
        // Continue asking for user input until the user inputs a 9
        while(!userInput.equals("9")) {
        	
        	switch (userInput) {
        		case "1":
        			// Option 1: Load all of our course data from the database to the CourseGraph
        			System.out.println("Type the full path to the .csv file you would like to upload.");
        			System.out.println("Example: C:\\Users\\Michael\\eclipse-workspace\\course-prerequisite-manager\\src\\main\\java\\com\\kibler\\capstone\\course_prerequisite_manager\\test.csv");        			
        			userInput = scanner.nextLine();
        			dbManager.loadCSV(userInput);
        			graph.loadGraph(dbManager);
        			System.out.println("Successfully Loadaded all course data from csv file to the database!");
        			break;
        		case "2":
        			// Option 2: Print out all the courses loaded into the database
        			System.out.println("Successfully chose option 2");
    				printCourses();
        			break;
        		case "3":
        			// Option 3: Choose a course to print out prereqs
        			printCourse(userInput, scanner);
        			break;
        		// Catch any input other than the expected values
        		default:
        			System.err.println("You have entered an invalid code. Please choose from the options provided in the menu.");
        		
        	}
        	
        	// Redisplay Application UI/Menu
        	displayMenu();
        	userInput = scanner.nextLine();
        }
        
        System.out.println("Successfully chose option 9");
        System.out.println("Application shutting down. Thank you!");
        
        // Close the scanner for memory management
        scanner.close();
        dbManager.closeDB();        
    }
    
    /*
     * Displays the User Interface menu for the user to interact with
     */
    public static void displayMenu() {
        System.out.println("\nMenu Options:");
        System.out.println("1. Load course data from csv");
        System.out.println("2. Print alphanumerically ordered list of all courses");
        System.out.println("3. Print course title and prerequisites");
        
        System.out.println("9. Exit");
        System.out.println("\nWhat would you like to do?");
    }
    
    /*
     * Prints the course name along with it's ID, followed by the suggest order of prerequisite courses
     * @param userInput The string input grabbed from the user
     */
    public static void printCourse(String userInput, Scanner scanner) {
    	
			System.out.println("Successfully chose option 3");
			System.out.println("\nPlease enter a valid courseID to continue, or 9 to return to main menu.");
			
			// Grab user input and capitalize
			userInput = scanner.nextLine().toUpperCase();
			
			// Loop until the user enters '9' to exit the application
			while(!userInput.equals("9")) {
				
				// If the user has not loaded data, kick them back to menu w/ instructions
				if (graph.getGraph().isEmpty()) {
					System.err.println("\nThe graph has not been loaded with course data from the database quite yet."
							+ "\nPlease return to the main menu and start with the 1st option.");
					break;
				}
				// If the user loaded data, but the is no matching courseID in the database, ask user to re-enter a courseID or exit with '9'
				else if (dbManager.getCoursesTable().getCourse(userInput) == null) {
					System.err.println("\nInvalid courseID. No matching courseID in the courses Database. "
							+ "\nPlease enter a valid courseID or \"9\" to continue.");
					userInput = scanner.nextLine().toUpperCase();
				} 
				// If the user has loaded data and there is a matching courseID, call 
				//	CourseGraph.getPrerequisiteOrder(Course selectedCourse) method to grab prereqs 
				else {
					Course selectedCourse = dbManager.getCoursesTable().getCourse(userInput);
					List<Course> prereqOrder = graph.getPrerequisiteOrder(selectedCourse);							// Recursively grab each course's prereqs
					
					System.out.println("\nYou selected " + selectedCourse.getName() + ", "
							+ "with the matching courseID of " + selectedCourse.getID() + ".\n");
			
					if (prereqOrder.isEmpty()) {
						System.out.println("No prerequisites for this course.");
					} 
					else {
						System.out.println("Suggested prerequisite order for " + selectedCourse.getID() + ":");
						for (Course course : prereqOrder) {
							System.out.println(course.getID() + " - " + course.getName());
						}
					}
					break;
				}
			}
    }
    
    public static void printCourses() {
		if (graph.getGraph().isEmpty()) {
			System.err.println("\nThe graph has not been loaded with course data from the database quite yet."
					+ "\nPlease return to the main menu and start with the 1st option.");
		}
		else {
			graph.printSorted();
		}
    }
}
