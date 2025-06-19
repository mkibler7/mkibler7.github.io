package com.kibler.capstone.course_prerequisite_manager.database;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import com.kibler.capstone.course_prerequisite_manager.Course;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/* 
 *	Class to interface an SQLite database with
 *	our application. 
 * 
 */

public class CourseDatabaseManager {

	private Connection conn;
	private static final String db_URL = "jdbc:sqlite:courses.db";
	private CoursesTable coursesTable;
	private PrerequisitesTable prereqsTable;
    
	public Connection getConnection() {
		return conn;
	}
	
	// @result: Create the courses and prerequisites tables
    public void onCreate() {
    	coursesTable.createTable();
    	prereqsTable.createTable();
    } 
    
    // @result: Create a connection to the database
	public void openDB() {
		try {
			conn = DriverManager.getConnection(db_URL);
			System.out.println("✅ Connected to SQLite");
			
			Statement stmt = conn.createStatement();
			stmt.execute("PRAGMA foreign_keys = ON;");
			
			// Create Table Objects
			coursesTable = new CoursesTable(conn);
			prereqsTable = new PrerequisitesTable(conn, coursesTable);
		}
		catch (SQLException e) {
			System.err.println("❌ Database Connection Failed: " + e.getMessage());
		}
	}
    
	// @result: Close the connection to the database
    public void closeDB() {
    	try {
    		if (conn != null && !conn.isClosed()) {
    			conn.close();
    			System.out.println("✅ Successfully disconnected from SQLite");
    		}
    	} catch (SQLException e) {
    		System.err.println("❌  Disconnection from Database Failed: " + e.getMessage());
    	}
    }
	
    // @return: Instance of coursesTable
    public CoursesTable getCoursesTable() {
    	return coursesTable;
    }
    
    // @return: Instance of prereqsTable
    public PrerequisitesTable getPrerequisitesTable() {
    	return prereqsTable;
    }
    
    // @return: A list of course objects for all records in the coursesTable
    public List<Course> getCourses() {
    	
    	List<Course> courses = new ArrayList<Course>();
    	String query = "SELECT * FROM courses";
    	
    	try (Statement stmt = conn.createStatement();
    			ResultSet rs = stmt.executeQuery(query)) {
    		
    		// Check if there are courses in database
    		if (rs.getString("course_id") == null) {
    			System.out.println("No current records in Course Table\n");
    			return null;
    		}
    		
    		// If there are, add to the courses list
    		while (rs.next()) {
    			String id = rs.getString("course_id");
    			String name = rs.getString("course_name");
    			
    			List<String> prereqIDs = prereqsTable.getPrereqIDs(id);
    			
    			courses.add(new Course(id, name, prereqIDs));
    		}
    		return courses;
    		
    	} catch (SQLException e) {
    		System.err.println("❌ CoursesTable.getCourses() Failed to run: " + e.getMessage());
    		return null;
    	}
    }
    
    /*
     *  @param: A String with a path to a .csv file 
     *  @result: Load data from the given .csv file to the database
     */
    
    public void loadCSV(String fileName) {
    	
    	//String directory = "C:\\Users\\Michael\\eclipse-workspace\\course-prerequisite-manager\\src\\main\\java\\com\\kibler\\capstone\\course_prerequisite_manager\\";
    	int length = fileName.length();
    	
    	// C:\d\f.csv
    	
    	if (length < 10) {
    		System.err.println("\nPlease ensure the format of your pathway is correct. It should be at minimum 10 characters. ex: C:\\d\\f.csv");
    		return;
    	}
    	
    	// Ensure the file is a .csv file
    	if (fileName.charAt(length - 3) == 'c' && 
    			fileName.charAt(length - 2) == 's' &&
    			fileName.charAt(length - 1) == 'v') 
    	{
    		// Throw the file into a scanner to read contents
    		try {
    			File file = new File(fileName);
				Scanner scanner = new Scanner(file);
			
				while (scanner.hasNextLine()) {
					
					String line = scanner.nextLine();
					
					String[] tokens = line.split(",");
					
					// Every first two elements should be the courseID and the matching name
					String courseID = tokens[0];
					String courseName = tokens[1];
					
					// Every element after, are courseIDs matching the prereqs for the course being added
					ArrayList<String> prereqs = new ArrayList<String>();
					for (int i = 2; i < tokens.length; i++) {
						prereqs.add(tokens[i]);
					}
					
					coursesTable.addCourse(courseID, courseName);
					
					for (String prereqID : prereqs) {
						prereqsTable.addPrereq(courseID, prereqID);
					}
				
				}
				
				// Close scanner for memory management
				scanner.close();
			} catch (FileNotFoundException e) {
				System.err.println("No file with that name in directory: " + e.getMessage());
			}
    	}
    	else {
    		System.err.println("The file must be a .csv file to continue");
    	}
    }
    
    /*
     * @result: Remove SQLTable data(for testing)
     */
    public void dropTables() {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS prerequisites;");
            stmt.execute("DROP TABLE IF EXISTS courses;");
            System.out.println("✅ Tables dropped successfully.");
        } catch (SQLException e) {
            System.err.println("❌ Failed to drop tables: " + e.getMessage());
        }
    }
    
}
