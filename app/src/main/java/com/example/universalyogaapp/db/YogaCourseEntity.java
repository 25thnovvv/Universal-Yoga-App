package com.example.universalyogaapp.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;

@Entity(tableName = "courses")
public class YogaCourseEntity {
    // Primary key for local database
    @PrimaryKey(autoGenerate = true)
    public int localDatabaseId;

    // Cloud synchronization
    public String cloudDatabaseId;
    public boolean cloudSyncStatus;

    // Course information
    public String courseName;
    public String weeklySchedule;
    public String classTime;
    public String instructorName;
    public String courseDescription;
    public String additionalNotes;
    public String nextClassDate;
    public int maxStudents;
    public int sessionDuration;
    public double coursePrice;

    // Default constructor
    public YogaCourseEntity() {}

    // Comprehensive constructor
    public YogaCourseEntity(int localDatabaseId, String cloudDatabaseId, String courseName, 
                       String weeklySchedule, String classTime, String instructorName, 
                       String courseDescription, String additionalNotes, String nextClassDate, 
                       int maxStudents, int sessionDuration, double coursePrice, boolean cloudSyncStatus) {
        this.localDatabaseId = localDatabaseId;
        this.cloudDatabaseId = cloudDatabaseId;
        this.courseName = courseName;
        this.weeklySchedule = weeklySchedule;
        this.classTime = classTime;
        this.instructorName = instructorName;
        this.courseDescription = courseDescription;
        this.additionalNotes = additionalNotes;
        this.nextClassDate = nextClassDate;
        this.maxStudents = maxStudents;
        this.sessionDuration = sessionDuration;
        this.coursePrice = coursePrice;
        this.cloudSyncStatus = cloudSyncStatus;
    }

    // Primary getters and setters
    public int getLocalDatabaseId() { return localDatabaseId; }
    public void setLocalDatabaseId(int localDatabaseId) { this.localDatabaseId = localDatabaseId; }

    public String getCloudDatabaseId() { return cloudDatabaseId; }
    public void setCloudDatabaseId(String cloudDatabaseId) { this.cloudDatabaseId = cloudDatabaseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getWeeklySchedule() { return weeklySchedule; }
    public void setWeeklySchedule(String weeklySchedule) { this.weeklySchedule = weeklySchedule; }

    public String getClassTime() { return classTime; }
    public void setClassTime(String classTime) { this.classTime = classTime; }

    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    public String getCourseDescription() { return courseDescription; }
    public void setCourseDescription(String courseDescription) { this.courseDescription = courseDescription; }

    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }

    public String getNextClassDate() { return nextClassDate; }
    public void setNextClassDate(String nextClassDate) { this.nextClassDate = nextClassDate; }

    public int getMaxStudents() { return maxStudents; }
    public void setMaxStudents(int maxStudents) { this.maxStudents = maxStudents; }

    public int getSessionDuration() { return sessionDuration; }
    public void setSessionDuration(int sessionDuration) { this.sessionDuration = sessionDuration; }

    public double getCoursePrice() { return coursePrice; }
    public void setCoursePrice(double coursePrice) { this.coursePrice = coursePrice; }

    public boolean getCloudSyncStatus() { return cloudSyncStatus; }
    public void setCloudSyncStatus(boolean cloudSyncStatus) { this.cloudSyncStatus = cloudSyncStatus; }

    // Legacy getters for backward compatibility
    public int getLocalId() { return localDatabaseId; }
    public void setLocalId(int localId) { this.localDatabaseId = localId; }

    public String getFirebaseId() { return cloudDatabaseId; }
    public void setFirebaseId(String firebaseId) { this.cloudDatabaseId = firebaseId; }

    public String getName() { return courseName; }
    public void setName(String name) { this.courseName = name; }

    public String getSchedule() { return weeklySchedule; }
    public void setSchedule(String schedule) { this.weeklySchedule = schedule; }

    public String getTime() { return classTime; }
    public void setTime(String time) { this.classTime = time; }

    public String getTeacher() { return instructorName; }
    public void setTeacher(String teacher) { this.instructorName = teacher; }

    public String getDescription() { return courseDescription; }
    public void setDescription(String description) { this.courseDescription = description; }

    public String getNote() { return additionalNotes; }
    public void setNote(String note) { this.additionalNotes = note; }

    public String getUpcomingDate() { return nextClassDate; }
    public void setUpcomingDate(String upcomingDate) { this.nextClassDate = upcomingDate; }

    public int getCapacity() { return maxStudents; }
    public void setCapacity(int capacity) { this.maxStudents = capacity; }

    public int getDuration() { return sessionDuration; }
    public void setDuration(int duration) { this.sessionDuration = duration; }

    public double getPrice() { return coursePrice; }
    public void setPrice(double price) { this.coursePrice = price; }

    public boolean isSynced() { return cloudSyncStatus; }
    public void setSynced(boolean synced) { this.cloudSyncStatus = synced; }
} 