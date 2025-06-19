package com.kibler.capstone.course_prerequisite_manager;

import java.util.List;

/**
 * Course class represents a single course with ID, name, and its prerequisites.
 */

public class Course {
	
	// Instance Variables
	private String courseID;
	private String courseName;
	private List<String> prerequisites;
	
	// Constructor
	public Course(String courseID, String courseName, List<String> prerequisites) {
		this.courseID = courseID;
		this.courseName = courseName;
		this.prerequisites = prerequisites;
	}
	
	// Getters
	public String getID() {
		return courseID;
	}
	public String getName() {
		return courseName;
	}
	public List<String> getPrerequisites() {
		return prerequisites;
	}
	
	public void print() {
		System.out.println("CourseID: " + courseID + " | CourseName: " + courseName);
	}
	
	
    /**
     * Override equals to ensure two Course objects are equal if their IDs match.
     * This allows Course objects to be used properly as keys in hash-based collections.
     */
	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
	        return true;
	    if (obj == null || getClass() != obj.getClass())
	        return false;
	    Course course = (Course) obj;
	    return courseID.equals(course.courseID);
	}

    /**
     * Override hashCode to match equals definition (based on course ID).
     * Required for correct behavior in hash-based collections.
     */
	@Override
	public int hashCode() {
	    return courseID.hashCode();
	}
	
}