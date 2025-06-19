package com.kibler.capstone.course_prerequisite_manager;
import com.kibler.capstone.course_prerequisite_manager.database.CourseDatabaseManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * CourseGraph is responsible for building and traversing the course prerequisite structure.
 * It allows determining the correct order in which a student should take courses.
 */


public class CourseGraph {

	// Map to store the course graph: course → list of its direct prerequisite courses
	private Map<Course, ArrayList<Course>> graph = new HashMap<Course, ArrayList<Course>>();
	
    /**
     * Loads all courses and prerequisites from the database and builds the in-memory graph structure.
     * 
     * @param dbManager The database manager instance used to retrieve course data.
     */
	public void loadGraph(CourseDatabaseManager dbManager) {
		
		//whitespace for testing purposes
		System.out.println();
		
        // Retrieve all courses from the database
		List<Course> courses = dbManager.getCourses();
		
		if (courses != null) {
			// Build the graph map
			courses.forEach(course -> {
				graph.put(course, new ArrayList<Course>());
				
				// For each prereqID, get the full Course object and add it to the adjacency list
				course.getPrerequisites().forEach(prereqID -> {
	                Course prereqCourse = dbManager.getCoursesTable().getCourse(prereqID);
	                if (prereqCourse != null) {
	                    graph.get(course).add(prereqCourse);
	                }
				});
			});
		}
	}
	
	/*
	 * Grabs instance of CourseGraph
	 */
	public Map<Course, ArrayList<Course>> getGraph() {
		return graph;
	}
	

    /**
     * Sorts and prints all of the courses in alpha numeric order.
     */
	public void printSorted() {
		
		// Create whitespace before printing
		System.out.println();
		
		// Throw all courses into an ArrayList
		ArrayList<Course> courses = new ArrayList<>(graph.keySet());
		
		// Sort in alphanumeric order
		Collections.sort(courses, Comparator.comparing(course -> course.getID()));
		
		System.out.println("Current courses available for enrollment:");
		
		// Print each CourseID with it's courseName
		for (Course course : courses) {
			System.out.println("CourseID: " + course.getID() + " | Course Name: " + course.getName());
		}
	}
	
    /**
     * Returns the valid prerequisite order for a specific course using localized topological sort.
     * 
     * @param startCourse The course the student wants to take.
     * @return List of courses in the correct order to take.
     */
	public List<Course> getPrerequisiteOrder(Course course) {
		List<Course> orderedCourses = new ArrayList<>();
		Set<Course> visited = new HashSet<>();
		
		// Use our dfs helper to recursively look at prereq chain
		dfs(course, visited, orderedCourses);

		// Remove the course object we are lookg at, as it is not a prereq
		orderedCourses.remove(course);
		
        return orderedCourses;
		
	}
	
    /**
     * Helper method: Post-order DFS for topological sorting.
     */
	private void dfs(Course course, Set<Course> visited, List<Course> orderedCourses) {
		
		if (visited.contains(course)) {
			return;
		}
		
		visited.add(course);
		
		for (Course prereq : graph.get(course)) {
			dfs(prereq, visited, orderedCourses);
		}
		
		orderedCourses.add(course);
	}
}
