package com.kibler.capstone.course_prerequisite_manager.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.kibler.capstone.course_prerequisite_manager.Course;

public class CoursesTable {
	
	private Connection conn;
    private static final String TABLE = "courses";
    private static final String COL_ID = "course_id";
    private static final String COL_NAME = "course_name";
    
    public CoursesTable(Connection conn) {
    	this.conn = conn;
    }
    
    /*	CREATE
     *  @param: Course object
     *  Result: Add course to the "courses" SQL table
     *  		Add course prereqs to the prereqs table
     */
    public void addCourse(String course, String name) {
    	
       	if (getCourse(course) != null) {
    		System.err.println("❌ Failed to add course: Course already in the database.");
    		return;
    	}
    	
    	String query = "INSERT INTO " + TABLE + "(" + COL_ID + ", " + COL_NAME + ") VALUES (?, ?)";
    	
    	try (PreparedStatement stmt = conn.prepareStatement(query)) {
    		stmt.setString(1, course);
    		stmt.setString(2, name);
    		stmt.executeUpdate();
    		System.out.println("✅ CoursesTable.addCourse Query Succes: Course Inserted to Course Table");
    	} catch (SQLException e) {
    		System.err.println("❌ CoursesTable.addCourse Query Failed: " + e.getMessage());
    	}
    }
    
    /*	READ
     * 	@param: String courseID: unique courseID
     * 	Result: Return a course object from the courses SQL table matching the courseID given.
     * 		   If no course object is returned, return null
     */
    public Course getCourse(String courseID) {
    	String query = "Select * FROM courses WHERE course_id = ?";
    	
    	try (PreparedStatement stmt = conn.prepareStatement(query)) {
    		
    		stmt.setString(1, courseID);
    		
    		try (ResultSet rs = stmt.executeQuery()) {
    			if (rs.next()) {
    				String id = rs.getString("course_id");
    				String name = rs.getString("course_name");
    				return new Course(id, name, new ArrayList<>());
    			}
    		}
    		
    	} catch (SQLException e) {
    		System.err.println("❌ getCourse query Failed: " + e.getMessage());
    	}
    	
    	return null;
    }
    
    
    /*	DELETE
     *  @param: Course object
     *  Result: Remove a course from the courses SQL table
     */
    public void deleteCourse(Course course) {
      	if (getCourse(course.getID()) == null) {
    		System.err.println("❌ CoursesTable.deleteCourse Failed to remove course: Course is not in the database.");
    		return;
    	}
      	
      	String query = "DELETE FROM " + TABLE + " WHERE " + COL_ID + " = ?";
      	
      	try(PreparedStatement stmt = conn.prepareStatement(query)) {
      		stmt.setString(1, course.getID());
      		stmt.executeUpdate();
    		System.out.println("✅ CoursesTable.deleteCourse Query Succes: Course removed from courses Table");
      	} catch (SQLException e) {
      		System.err.println("❌ CoursesTable.deleteCourse Query Failed: " + e.getMessage());
      	}
    }
    
    // @result: Create an SQL table for courses
    public void createTable() {
    	String createCourses = "CREATE TABLE IF NOT EXISTS " + TABLE + " (" +
        		COL_ID + " TEXT PRIMARY KEY, " +
        		COL_NAME + " TEXT NOT NULL" +
        		");";
    	
        try (Statement stmt = conn.createStatement()) {
        	stmt.execute(createCourses);
        	System.out.println("✅ CoursesTable.onCreate Query Successful: courses Table created");
        } catch (SQLException e) {
        	System.err.println("❌ CoursesTable.onCreate Query Failed: " + e.getMessage());
        }
    }
    
    // @result: Print the data records from the coursesTable
    public void print() {
    	String query = "SELECT * FROM courses";
    	
    	try (Statement stmt = conn.createStatement();
    			ResultSet rs = stmt.executeQuery(query)) {
    		
    		System.out.println("\nCourses Table:");	
    		
    		// Check if there are courses in database
    		if (rs.getString("course_id") == null) {
    			System.out.println("No current records in Course Table\n");
    			return;
    		}
    		
    		// If there are, print all data to console
    		while (rs.next()) {
    			String id = rs.getString("course_id");
    			String name = rs.getString("course_name");
    			System.out.println("CourseID: " + id + " | Name: " + name);
    		}
    	} catch (SQLException e) {
    		System.err.println("❌ CoursesTable.print() Failed to print table " + e.getMessage());
    	}
    }
}
