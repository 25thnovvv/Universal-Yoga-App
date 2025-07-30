package com.example.universalyogaapp.model;

import java.io.Serializable;

public class ClassInstance implements Serializable {
    // Instance identification
    private String instanceId;
    private String parentCourseId;
    private int localDatabaseId;
    
    // Class details
    private String classDate;
    private String assignedInstructor;
    private String classNotes;

    // Default constructor for Firebase serialization
    public ClassInstance() {}

    // Comprehensive constructor
    public ClassInstance(String instanceId, String parentCourseId, String classDate, 
                        String assignedInstructor, String classNotes, int localDatabaseId) {
        this.instanceId = instanceId;
        this.parentCourseId = parentCourseId;
        this.classDate = classDate;
        this.assignedInstructor = assignedInstructor;
        this.classNotes = classNotes;
        this.localDatabaseId = localDatabaseId;
    }

    // Primary getters and setters
    public String getInstanceId() { return instanceId; }
    public void setInstanceId(String instanceId) { this.instanceId = instanceId; }

    public String getParentCourseId() { return parentCourseId; }
    public void setParentCourseId(String parentCourseId) { this.parentCourseId = parentCourseId; }

    public String getClassDate() { return classDate; }
    public void setClassDate(String classDate) { this.classDate = classDate; }

    public String getAssignedInstructor() { return assignedInstructor; }
    public void setAssignedInstructor(String assignedInstructor) { this.assignedInstructor = assignedInstructor; }

    public String getClassNotes() { return classNotes; }
    public void setClassNotes(String classNotes) { this.classNotes = classNotes; }

    public int getLocalDatabaseId() { return localDatabaseId; }
    public void setLocalDatabaseId(int localDatabaseId) { this.localDatabaseId = localDatabaseId; }

    // Legacy getters for backward compatibility
    public String getId() { return instanceId; }
    public void setId(String id) { this.instanceId = id; }

    public String getCourseId() { return parentCourseId; }
    public void setCourseId(String courseId) { this.parentCourseId = courseId; }

    public String getDate() { return classDate; }
    public void setDate(String date) { this.classDate = date; }

    public String getTeacher() { return assignedInstructor; }
    public void setTeacher(String teacher) { this.assignedInstructor = teacher; }

    public String getNote() { return classNotes; }
    public void setNote(String note) { this.classNotes = note; }

    public int getLocalId() { return localDatabaseId; }
    public void setLocalId(int localId) { this.localDatabaseId = localId; }
}