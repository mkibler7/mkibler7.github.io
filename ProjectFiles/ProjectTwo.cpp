//============================================================================
// Name            : ProjectTwo.cpp (Course Pre
// Author          : Michael Kibler
// Description     : Final Project
// Start Date      : 8/15
// Completion Date : 8/17
//============================================================================

#include <iostream>
#include <vector>
#include <string> // atoi
#include <fstream>
#include <sstream>
#include <cctype>
#include <algorithm>



using namespace std;

//============================================================================
// Global definitions visible to all methods and classes
//============================================================================

const unsigned int DEFAULT_SIZE = 179;

// define a structure to hold course information
struct Course {
    string courseNumber; // unique identifier
    string name;
    vector<string> prerequisites = {};

    Course() = default;

    Course(vector<string> data) {
        this->courseNumber = data[0];
        this->name = data[1];

        if (data.size() > 2) {
            for (size_t i = 2; i < data.size(); i++) {
                if (data[i] != "") {
                    this->prerequisites.push_back(data[i]);
                }
            }
        }
    }

    void printInfo() {
        cout << "\nCourse Number: " << courseNumber << endl;
        cout << "Course Name: " << name << endl;
        cout << "Prerequisites Vector Size: " << prerequisites.size() << endl;
        for (const string prerequisite : prerequisites) {
            cout << "Prereq: " << prerequisite << endl;
        }

        cout << endl;
    }
};


// Hash Table class definition
//=======//=================================================================================================================================================

/**
 * Define a class containing data members and methods to
 * implement a hash table with chaining.
 */

class HashTable {

private:
    // Define structures to hold bids
    struct Node {
        Course course;
        unsigned int key;
        Node* next;

        // default constructor
        Node() {
            key = UINT_MAX;
            next = nullptr;
        }

        // initialize with a bid
        Node(Course aCourse) : Node() {
            course = aCourse;
        }

        // initialize with a bid and a key
        Node(Course aCourse, unsigned int aKey) : Node(aCourse) {
            key = aKey;
        }
    };

    vector<Node> nodes;

    unsigned int tableSize = DEFAULT_SIZE;
    int numberOfCourses = 0;

    unsigned int hash(string key);

public:
    HashTable();
    HashTable(unsigned int size);
    virtual ~HashTable();
    void Insert(Course course);
    void PrintAll();
    void printSchedule();
    void SortList(vector<Node>& courseList);
    void printClass(string courseNumber);
    void Remove(string courseNumber);
    Course Search(string courseNumber);
    void loadCourses(string fileName);
};


/**
 * Default constructor
 */

HashTable::HashTable() {

    // Initalize node structure by resizing tableSize
    nodes.resize(tableSize);
}

/**
 * Constructor for specifying size of the table
 * Use to improve efficiency of hashing algorithm
 * by reducing collisions without wasting memory.
 */

HashTable::HashTable(unsigned int size) {
    // invoke local tableSize to size with this->
    tableSize = size;

    // resize nodes size
    nodes.resize(tableSize);
}

/**
 * Destructor
 */

HashTable::~HashTable() {

    // erase nodes beginning
    nodes.erase(nodes.begin());
}

/**
 * Calculate the hash value of a given key.
 * Note that key is specifically defined as
 * unsigned int to prevent undefined results
 * of a negative list index.
 *
 * @param key The key to hash
 * @return The calculated hash
 */

unsigned int HashTable::hash(string key) {
    // Logic to calculate a hash value
    unsigned int hashValue = 0;
    for (char c : key) {
        hashValue = (hashValue * 31) + c;  // 31 because it is a prime #(reducing collisions)
    }

    // return key  tableSize
    return hashValue % tableSize;
}


/**
 * Insert a bid
 *
 * @param bid The bid to insert
 */

void HashTable::Insert(Course course) {
    // Logic to insert a bid
    // Create the key for the given bid
    unsigned key = hash(course.courseNumber);

    // retrieve node using key
    Node* oldNode = &(nodes.at(key));


    if (oldNode->key == UINT_MAX) {
        // assing old node key to UNIT_MAX, set to key, set old node to course and old node next to null pointer
        oldNode->key = key;
        oldNode->course = course;
        oldNode->next = nullptr;
    }
    // else find the next open node
    else {
       while (oldNode->next != nullptr) {
           oldNode = oldNode->next;
       }
        // add new newNode to end
        oldNode->next = new Node(course, key);
    }
    
    numberOfCourses++;
}

/*
    TASK #1 & #3 Reading data file & Loading Data Structure : Develop working code
    to load data from the file into the data structure
    Input: CSV fileName as a string
    Output: HashTable is loaded with data from CSV file
*/
void HashTable::loadCourses(string fileName) {

    // Open the file for reading
    ifstream inputFile(fileName);

    // Check to make sure inputFile stream was properly created
    if (!inputFile) {
        cerr << "There was an error opening the file for reading." << endl;
        exit;
    }

    // Loop over each line of file
    string line;
    while (getline(inputFile, line)) {

        // Create a stream from the string 
        istringstream stream(line);
        string token;
        vector<string> data;

        // Split line into tokens split by ',' and add it to our vector
        while (getline(stream, token, ',')) {
            data.push_back(token);
        }

        // Check to make sure there are at least 2 tokens per line
        if (data.size() < 2) {
            cerr << " Error: Each line must have at least a course number and a name" << endl;
            continue;
        }

        // If there are more than two values in the data 
        bool validPrereqs = true;
        if (data.size() > 2) {

            // Validate and collect prereqs
            for (size_t i = 2; i < data.size(); i++) {
                string prerequisite = data[i];
                bool valid = false;

                // Create stream to capture first token of each line
                ifstream file(fileName);
                string fileLine;

                // Loop over ever line of file to get the first token
                while (getline(file, fileLine)) {
                    istringstream ss(fileLine);
                    string firstToken;

                    // Extract only first token of each line
                    if (getline(ss, firstToken, ',')) {

                        // If any of the tokens match the prereq, it is valid
                        if (firstToken == prerequisite) {
                            valid = true;
                            break; // Exit loop if prereq is found
                        }
                    }
                }

                // Close file to garabage collect
                file.close();

                // If the prereq is not a valid class, break loop cycle to
                // Prevent it from being added to our HashTable
                if (!valid && !prerequisite.empty()) {
                    cerr << "Error: Prerequisite " << prerequisite << "not found in the file." << endl;
                    validPrereqs = false;
                    break;
                }
            }
        }
        // If a prerequisite is NOT in the file, continue to next line
        if (!validPrereqs) {
            continue;
        }

        // Create new Course object with data from line
        Course newCourse = Course(data);

        // Add new course object to course hashtable
        this->Insert(newCourse);
    }

    // Close file to garabage collect
    inputFile.close();
}

/*
    TASK #4 - Course List : Develop working code to sort and print out
    a list of the courses in the Computer Science program in alphanumeric order
*/
void HashTable::printSchedule() {

    // if hashtable is empty output message and return
    if (this->numberOfCourses == 0) {
        cerr << "HashTable is empty. please load data from a csv file into HashTable before trying to print." << endl;
        return;
    }
    

    // Create a temp vector and fill with elements from nodes vector
    vector<Node> sortedCourses;
    for (const Node& node : nodes) {
        if (node.key != UINT_MAX) {
            sortedCourses.push_back(node);
            Node* current = node.next;
            while (current != nullptr) {
                sortedCourses.push_back(*current);
                current = current->next;
            }
        }
    }

    // Sort the vector copy
    SortList(sortedCourses);

    for (const Node& node : sortedCourses) {
        cout << node.course.courseNumber << ", " << node.course.name << endl;
    }
}

// TASK #4 Continued
// Sort nodes vector in alphanumeric order
void HashTable::SortList(vector<Node>& courseList) {

    // Loop over each element in the courseList
    for (size_t i = 0; i < courseList.size(); i++) {
        unsigned int key = courseList.at(i).key;
        Node tempNode = courseList.at(i);
        int j = i - 1;

        while ((j >= 0) && courseList[j].course.courseNumber > tempNode.course.courseNumber) {
            courseList[j + 1] = courseList[j];
            j = j - 1;
        }

        courseList[j + 1] = tempNode;
    }
}


// TASK #5 - Course Information: Develop working code to print course information
void HashTable::printClass(string courseNumber) {

    // Ensure the courseNumber is uppercase, to ensure proper matching with HashTable elements
    transform(courseNumber.begin(), courseNumber.end(), courseNumber.begin(), ::toupper);

    // Grab the course object from the HashTable
    Course course = this->Search(courseNumber);

    // If the courseNumber returns a Course from the Course HashTable
    if (course.courseNumber != "") {
        // Print out the Course information to console
        cout << courseNumber << ", " << course.name << endl;

        cout << "Prerequisites: ";

        if (course.prerequisites.size() == 0) {
            cout << "No prerequisites required for this course!";
        }

        for (size_t i = 0; i < course.prerequisites.size(); i++) {
            cout << course.prerequisites[i];
            
            if (i != (course.prerequisites.size() - 1)) {
                cout << ", ";
            }
        }
    }
    else {
        // If the coureNumber does not return a Course from the HashTable, print error
        cerr << "Error: the courseNumber you entered is not in the HashTable" << endl;
    }
    cout << endl;
}

/*
 * Remove a Course
 * NOT REQUIRED FOR PROJECT BUT KEEPING FOR FUNCTIONALITY IF NEEDED FOR TESTING PURPOSES
 * @param courseNumber The courseNumber to search for 
 */
//
//void HashTable::Remove(string courseNumber) {
//
//    // Logic to remove a course
//    // set key equal to hash atoi courseNumber
//    unsigned int key = hash(courseNumber);
//    // erase node begin and key
//    // Retrieve the node using the key
//    Node* node = &nodes.at(key);
//    Node* previous = nullptr;
//
//    // If the node to be removed is the first node in the list
//    if (node->course.courseNumber == courseNumber) {
//        // If there are no chained nodes, reset the node
//        if (node->next == nullptr) {
//            node->key = UINT_MAX;
//            node->course = Course();
//        }
//        else {
//            // Otherwise, adjust the node to the next node in the chain
//            Node* temp = node->next;
//            *node = *temp;
//            delete temp;
//        }
//        return;
//
//    }
//
//    // Traverse the linked list to find the node to remove
//    while (node != nullptr && node->course.courseNumber != courseNumber) {
//        previous = node;
//        node = node->next;
//    }
//
//    // If the course was not found
//    if (node == nullptr) {
//        cout << "Course Number " << courseNumber << " not found." << endl;
//        return;
//    }
//
//    // Remove the node from the chain
//    previous->next = node->next;
//    delete node;
//    numberOfCourses--;
//}


/**
 * Search for the specified courseNumber
 *
 * @param The courseNumber to search for
 */

Course HashTable::Search(string courseNumber) {

    // Logic to search for and return a course
    // create the key for the given bid
    unsigned int key = hash(courseNumber);

    // Start with the node at the hash index
    Node* node = &nodes.at(key);
    
    // while node not equal to nullptr
    while (node != nullptr && node->key != UINT_MAX) {

        // if the current node matches, return it
        if (node->course.courseNumber == courseNumber) {
            return node->course;
        }

        //node is equal to next node
        node = node->next;
    }

    // return empty course if no entry is found
    return Course();
}


int main()
{
    cout << "Welcome to the course planner.\n" << endl;

    // TASK #1 - Input: Design code to correctly read the course data file
    string fileName;

    // Project CSV file name: CS_300_ABCU_Advising_Program_Input.csv
    // Make sure the file is in the projet source folder
    cout << "Please input file name for the data file you would " <<
        "like to upload." << endl;
    cin >> fileName;
    
    // Initialize new HashTable
    HashTable courseTable = HashTable();
    int userChoice;
    bool menuFlag = true;

    while (menuFlag) {

        // TASK #2 - Menu: Design code to create a menu that prompts a user for menu options
        // Prompt user with menu
        cout << "\nMenu Options:" << endl;
        cout << "1. Load course data" << endl;
        cout << "2. Print alphanumerically ordered list of all courses" << endl;
        cout << "3. Print course title and prerequisites" << endl;
        cout << "9. Exit" << endl;
        cout << "\nWhat would you like to do?" << endl;

        // Capture user input with validation
        while (!(cin >> userChoice)) {
            cin.clear(); // Clear error flag
            cin.ignore(numeric_limits<streamsize>::max(), '\n'); // Discard invalid input
            cerr << "Invalid input. Please enter a number." << endl;
        }

        if (userChoice == 1) {

            // TASK #3 - Loading Data Structure: Develop working code to load data from the file 
            // into the data structure.
            courseTable.loadCourses(fileName);
            cout << "Loaded courses from " << fileName << "!" << endl;
        }
        else if (userChoice == 2) {

            // TASK #4 - Course List: Develop working code to sort and 
            // print out a list of the courses in the Computer Science program in alphanumeric order
            courseTable.printSchedule();
        }
        else if (userChoice == 3) {

            // TASK #5 - Course Information: Develop working code to print course information
            string userInput;
            cout << "What course do you want to know about?" << endl;
            cin >> userInput;

            courseTable.printClass(userInput);
        }
        else if (userChoice == 9) {

            // EXIT application option
            menuFlag = false;
            cout << "Thank you for using the course planner!" << endl;
        }
        else {
            cerr << userChoice << " is not a valid option. Please try again." << endl;
        }
    }


    return 0;
}