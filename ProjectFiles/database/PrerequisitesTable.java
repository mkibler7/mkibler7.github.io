package com.kibler.capstone.course_prerequisite_manager.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.kibler.capstone.course_prerequisite_manager.Course;

public class PrerequisitesTable {
	
	private Connection conn;
	private CoursesTable coursesTable;
    private static final String TABLE = "prerequisites";
    private static final String COL_ID = "course_id";
    private static final String COL_PREREQUISITE = "prerequisite_id";    
    
    
    public PrerequisitesTable(Connection conn, CoursesTable coursesTable) {
    	this.conn = conn;
    	this.coursesTable = coursesTable;
    }
    
    /*	CREATE
     * 	@param1: Course course
     * 	@param2: Course prerequisite
     * 	@return: Add a prerequisite to the prerequisite SQL Table
     */
    public void addPrereq(String course, String prereq) {

    	// Check to make sure course/prereq combo are not already in prerequisite table
       	if (inTable(course, prereq) != false) {
    		System.err.println("❌ Failed to add prereq: Prerequisite already in the database.");
    		return;
    	}
       	
       	// Check to make sure both course and prereq are in courses table
       	if (coursesTable.getCourse(course) == null || coursesTable.getCourse(prereq) == null) {
    		System.err.println("❌ Failed to add prereq: One, or both, of the courses are not in the courses table.");
    		return;
       	}
    	
    	String query = "INSERT INTO " + TABLE + "(" + COL_ID + ", " + COL_PREREQUISITE + ") VALUES (?, ?)";
    	
    	try (PreparedStatement stmt = conn.prepareStatement(query)) {
    		stmt.setString(1, course);
    		stmt.setString(2, prereq);
    		stmt.executeUpdate();
    		System.out.println("✅ Prerequisites.addPrereq Query Succes: Course Inserted to Course Table");
    	} catch (SQLException e) {
    		System.err.println("❌ Prerequisites.addPrereq Query Failed: " + e.getMessage());
    	}
    }
    
    /*	READ
     * 	@param: String courseID: unique courseID
     * 	@return: Return a list of course objects matching the courseID given
     * 		   	 If no course object is returned, return null
     */
    public ArrayList<String> getPrereqIDs(String courseID) {
    	String query = "Select * FROM " + TABLE + " WHERE " + COL_ID + " = ?";
    	ArrayList<String> prereqIDs = new ArrayList<String>();
    	
    	try (PreparedStatement stmt = conn.prepareStatement(query)) {
    		
    		stmt.setString(1, courseID);
    		
    		try (ResultSet rs = stmt.executeQuery()) {
    			
    			while (rs.next()) {
    				String prereqID = rs.getString("prerequisite_id");
    				prereqIDs.add(prereqID);
    			}
    		}
    		
    	} catch (SQLException e) {
    		System.err.println("❌ PrerequisitesTable.getPrereqIDs query Failed: " + e.getMessage());
    	}
    	
    	return prereqIDs;
    }
    
    /*	READ
     * 	@param1: String courseID: unique courseID
     * 	@param2: String prereqID: unique courseID
     * 	@return: True if pair exists in table, False if not
     */
    public boolean inTable(String courseID, String prereqID) {
    	String query = "Select * FROM prerequisites WHERE course_id = ? AND prerequisite_id = ?";
    	
    	try (PreparedStatement stmt = conn.prepareStatement(query)) {
    		
    		stmt.setString(1, courseID);
    		stmt.setString(2, prereqID);
    		
    		try (ResultSet rs = stmt.executeQuery()) {
    			return rs.next();
    		}
    		
    	} catch (SQLException e) {
    		System.err.println("❌ getCourse query Failed: " + e.getMessage());
    	}
    	
    	return false;
    }
    
    /*	DELETE
     *  @param: Course course
     *  @param: Course prerequisite
     *  @return: Remove a course from the courses SQL table
     */
    public void deletePrereq(Course course, Course prereq) {
      	if (inTable(course.getID(), prereq.getID()) == false) {
    		System.err.println("❌ PrerequisitesTable.deletePrereq Failed to remove prerequisite: Not found in the database.");
    		return;
    	}
      	
      	String query = "DELETE FROM " + TABLE + " WHERE " + COL_ID + " = ? AND " + COL_PREREQUISITE + " = ?";
      	
      	try(PreparedStatement stmt = conn.prepareStatement(query)) {
      		stmt.setString(1, course.getID());
      		stmt.setString(2, prereq.getID());
      		stmt.executeUpdate();
    		System.out.println("✅ PrerequisitesTable.deletePrereq Query Succes: Prerequisite removed from prerequisites Table");
      	} catch (SQLException e) {
      		System.err.println("❌ PrerequisitesTable.deletePrereq Query Failed: " + e.getMessage());
      	}
    }
    
    // @result: create an SQLite table for prerequisites
    public void createTable() {
        String createPrereqs = "CREATE TABLE IF NOT EXISTS " + TABLE + " (" +
        		COL_ID + " TEXT NOT NULL, " +
        		COL_PREREQUISITE + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COL_ID + ") REFERENCES courses(course_id) ON DELETE CASCADE, " +
        		"FOREIGN KEY(" + COL_PREREQUISITE + ") REFERENCES courses(course_id));";
        
        
        try (Statement stmt = conn.createStatement()) {
        	stmt.execute(createPrereqs);
        	System.out.println("✅ PrerequisitesTable.onCreate Query Successful: courses Table created");
        } catch (SQLException e) {
        	System.err.println("❌ PrerequisitesTable.onCreate Query Failed: " + e.getMessage());
        }
    }
    
 // @result: Print data records from the prereqs table
    public void print() {
    	String query = "SELECT * FROM prerequisites";
    	
    	try (Statement stmt = conn.createStatement();
    			ResultSet rs = stmt.executeQuery(query)) {
    		
    		System.out.println("\nPrerequisites Table:");	
    		
    		// Check if there are prereqs in database
    		if (rs.getString("course_id") == null) {
    			System.out.println("No current records in Prerequisite Table\n");
    			return;
    		}
    		
    		// If there are, print all data to console
    		while (rs.next()) {
    			String courseID = rs.getString("course_id");
    			String prereqID = rs.getString("prerequisite_id");
    			System.out.println("CourseID: " + courseID + " | PrereqID: " + prereqID);
    		}
    	} catch (SQLException e) {
    		System.err.println("❌ PrerequisitesTable.print() Failed to print table " + e.getMessage());
    	}
    }
    
    /*
     *  @param: A list of prerequisite course objects
     *  @result: Print the course ID of all prereqs in the list
     */
    public void print(ArrayList<Course> prereqs) {
    	prereqs.forEach(course -> {
    		System.out.println(course.getID());
    	});
    }
}
