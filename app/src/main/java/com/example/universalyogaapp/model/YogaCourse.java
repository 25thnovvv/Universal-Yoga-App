package com.example.universalyogaapp.model;

import java.io.Serializable;

public class YogaCourse implements Serializable {
    // Core course information
    private String courseId;
    private String courseName;
    private String weeklySchedule;
    private String classTime;
    private String instructorName;
    private int maxStudents;
    private double coursePrice;
    private int sessionDuration;
    private String courseDescription;
    private String additionalNotes;
    private String nextClassDate;
    private int databaseId;

    // Default constructor for Firebase serialization
    public YogaCourse() {}

    // Comprehensive constructor
    public YogaCourse(String courseId, String courseName, String weeklySchedule, String classTime, 
                  String instructorName, int maxStudents, double coursePrice, int sessionDuration, 
                  String courseDescription, String additionalNotes, String nextClassDate, int databaseId) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.weeklySchedule = weeklySchedule;
        this.classTime = classTime;
        this.instructorName = instructorName;
        this.maxStudents = maxStudents;
        this.coursePrice = coursePrice;
        this.sessionDuration = sessionDuration;
        this.courseDescription = courseDescription;
        this.additionalNotes = additionalNotes;
        this.nextClassDate = nextClassDate;
        this.databaseId = databaseId;
    }

    // Getters and Setters
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    
    public String getWeeklySchedule() { return weeklySchedule; }
    public void setWeeklySchedule(String weeklySchedule) { this.weeklySchedule = weeklySchedule; }
    
    public String getClassTime() { return classTime; }
    public void setClassTime(String classTime) { this.classTime = classTime; }
    
    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
    
    public int getMaxStudents() { return maxStudents; }
    public void setMaxStudents(int maxStudents) { this.maxStudents = maxStudents; }
    
    public double getCoursePrice() { return coursePrice; }
    public void setCoursePrice(double coursePrice) { this.coursePrice = coursePrice; }
    
    public int getSessionDuration() { return sessionDuration; }
    public void setSessionDuration(int sessionDuration) { this.sessionDuration = sessionDuration; }
    
    public String getCourseDescription() { return courseDescription; }
    public void setCourseDescription(String courseDescription) { this.courseDescription = courseDescription; }
    
    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }
    
    public String getNextClassDate() { return nextClassDate; }
    public void setNextClassDate(String nextClassDate) { this.nextClassDate = nextClassDate; }
    
    public int getDatabaseId() { return databaseId; }
    public void setDatabaseId(int databaseId) { this.databaseId = databaseId; }

    // Legacy getters for backward compatibility
    public String getId() { return courseId; }
    public void setId(String id) { this.courseId = id; }
    
    public String getName() { return courseName; }
    public void setName(String name) { this.courseName = name; }
    
    public String getSchedule() { return weeklySchedule; }
    public void setSchedule(String schedule) { this.weeklySchedule = schedule; }
    
    public String getTime() { return classTime; }
    public void setTime(String time) { this.classTime = time; }
    
    public String getTeacher() { return instructorName; }
    public void setTeacher(String teacher) { this.instructorName = teacher; }
    
    public int getCapacity() { return maxStudents; }
    public void setCapacity(int capacity) { this.maxStudents = capacity; }
    
    public double getPrice() { return coursePrice; }
    public void setPrice(double price) { this.coursePrice = price; }
    
    public int getDuration() { return sessionDuration; }
    public void setDuration(int duration) { this.sessionDuration = duration; }
    
    public String getDescription() { return courseDescription; }
    public void setDescription(String description) { this.courseDescription = description; }
    
    public String getNote() { return additionalNotes; }
    public void setNote(String note) { this.additionalNotes = note; }
    
    public String getUpcomingDate() { return nextClassDate; }
    public void setUpcomingDate(String upcomingDate) { this.nextClassDate = upcomingDate; }
    
    public int getLocalId() { return databaseId; }
    public void setLocalId(int localId) { this.databaseId = localId; }
} 