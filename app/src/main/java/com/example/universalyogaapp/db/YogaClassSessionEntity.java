package com.example.universalyogaapp.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;

@Entity(tableName = "class_instances")
public class YogaClassSessionEntity {
    // Primary key for local database
    @PrimaryKey(autoGenerate = true)
    public int localDatabaseId;

    // Relationships and synchronization
    public String parentCourseId;
    public String cloudDatabaseId;
    public boolean cloudSyncStatus;
    public boolean isDeleted = false; // For soft deletion

    // Class session details
    public String classDate;
    public String assignedInstructor;
    public String classNotes;

    // Default constructor
    public YogaClassSessionEntity() {}

    // Comprehensive constructor
    public YogaClassSessionEntity(int localDatabaseId, String parentCourseId, String cloudDatabaseId, 
                              String classDate, String assignedInstructor, String classNotes, 
                              boolean cloudSyncStatus) {
        this.localDatabaseId = localDatabaseId;
        this.parentCourseId = parentCourseId;
        this.cloudDatabaseId = cloudDatabaseId;
        this.classDate = classDate;
        this.assignedInstructor = assignedInstructor;
        this.classNotes = classNotes;
        this.cloudSyncStatus = cloudSyncStatus;
        this.isDeleted = false; // Default value
    }

    // Getters and setters
    public int getLocalDatabaseId() { return localDatabaseId; }
    public void setLocalDatabaseId(int localDatabaseId) { this.localDatabaseId = localDatabaseId; }

    public String getParentCourseId() { return parentCourseId; }
    public void setParentCourseId(String parentCourseId) { this.parentCourseId = parentCourseId; }

    public String getCloudDatabaseId() { return cloudDatabaseId; }
    public void setCloudDatabaseId(String cloudDatabaseId) { this.cloudDatabaseId = cloudDatabaseId; }
    
    public boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }

    public String getClassDate() { return classDate; }
    public void setClassDate(String classDate) { this.classDate = classDate; }

    public String getAssignedInstructor() { return assignedInstructor; }
    public void setAssignedInstructor(String assignedInstructor) { this.assignedInstructor = assignedInstructor; }

    public String getClassNotes() { return classNotes; }
    public void setClassNotes(String classNotes) { this.classNotes = classNotes; }

    public boolean getCloudSyncStatus() { return cloudSyncStatus; }
    public void setCloudSyncStatus(boolean cloudSyncStatus) { this.cloudSyncStatus = cloudSyncStatus; }

    // Legacy getters for backward compatibility
    public int getId() { return localDatabaseId; }
    public void setId(int id) { this.localDatabaseId = id; }

    public String getCourseId() { return parentCourseId; }
    public void setCourseId(String courseId) { this.parentCourseId = courseId; }

    public String getFirebaseId() { return cloudDatabaseId; }
    public void setFirebaseId(String firebaseId) { this.cloudDatabaseId = firebaseId; }

    public String getDate() { return classDate; }
    public void setDate(String date) { this.classDate = date; }

    public String getTeacher() { return assignedInstructor; }
    public void setTeacher(String teacher) { this.assignedInstructor = teacher; }

    public String getNote() { return classNotes; }
    public void setNote(String note) { this.classNotes = note; }

    public boolean isSynced() { return cloudSyncStatus; }
    public void setSynced(boolean synced) { this.cloudSyncStatus = synced; }
} 
