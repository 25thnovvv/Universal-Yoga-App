package com.example.universalyogaapp.firebase;

import androidx.annotation.NonNull;

import com.example.universalyogaapp.model.YogaCourse;
import com.example.universalyogaapp.model.YogaClassSession;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class YogaFirebaseManager {
    // Firebase configuration
    private static final String FIREBASE_DATABASE_URL = "https://universayogaapp-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private final FirebaseDatabase firebaseDatabase;
    private final DatabaseReference coursesReference;
    private final DatabaseReference classSessionsReference;

    // Constructor
    public YogaFirebaseManager() {
        firebaseDatabase = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL);
        coursesReference = firebaseDatabase.getReference("courses");
        classSessionsReference = firebaseDatabase.getReference("class_instances");
    }

    // Course management methods
    public void createNewCourse(YogaCourse course, DatabaseReference.CompletionListener completionListener) {
        String generatedId = coursesReference.push().getKey();
        course.setId(generatedId);
        coursesReference.child(generatedId).setValue(course, completionListener);
    }

    public void fetchAllCourses(ValueEventListener valueEventListener) {
        coursesReference.addValueEventListener(valueEventListener);
    }

    public void fetchCourseById(String courseId, ValueEventListener valueEventListener) {
        coursesReference.child(courseId).addListenerForSingleValueEvent(valueEventListener);
    }

    public void updateExistingCourse(YogaCourse course, DatabaseReference.CompletionListener completionListener) {
        if (course.getId() == null) return;
        coursesReference.child(course.getId()).setValue(course, completionListener);
    }

    public void removeCourse(String courseId, DatabaseReference.CompletionListener completionListener) {
        coursesReference.child(courseId).removeValue(completionListener);
    }

    // Class session management methods
    public void createNewClassSession(YogaClassSession classSession, DatabaseReference.CompletionListener completionListener) {
        String generatedId = classSessionsReference.push().getKey();
        classSession.setId(generatedId);
        classSessionsReference.child(generatedId).setValue(classSession, completionListener);
    }

    public void fetchClassSessionsByCourseId(String courseId, ValueEventListener valueEventListener) {
        classSessionsReference.orderByChild("courseId").equalTo(courseId).addValueEventListener(valueEventListener);
    }

    public void updateExistingClassSession(YogaClassSession classSession, DatabaseReference.CompletionListener completionListener) {
        if (classSession.getId() == null) return;
        classSessionsReference.child(classSession.getId()).setValue(classSession, completionListener);
    }

    public void removeClassSession(String sessionId, DatabaseReference.CompletionListener completionListener) {
        classSessionsReference.child(sessionId).removeValue(completionListener);
    }

    public void removeAllClassSessionsByCourseId(String courseId, DatabaseReference.CompletionListener completionListener) {
        classSessionsReference.orderByChild("courseId").equalTo(courseId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    childSnapshot.getRef().removeValue();
                }
                if (completionListener != null) completionListener.onComplete(null, classSessionsReference);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (completionListener != null) completionListener.onComplete(databaseError, classSessionsReference);
            }
        });
    }

    // Legacy methods for backward compatibility
    public void addCourse(YogaCourse course, DatabaseReference.CompletionListener listener) {
        createNewCourse(course, listener);
    }

    public void getCourses(ValueEventListener listener) {
        fetchAllCourses(listener);
    }

    public void getCourseById(String courseId, ValueEventListener listener) {
        fetchCourseById(courseId, listener);
    }

    public void updateCourse(YogaCourse course, DatabaseReference.CompletionListener listener) {
        updateExistingCourse(course, listener);
    }

    public void deleteCourse(String courseId, DatabaseReference.CompletionListener listener) {
        removeCourse(courseId, listener);
    }

    public void addClassInstance(YogaClassSession session, DatabaseReference.CompletionListener listener) {
        createNewClassSession(session, listener);
    }

    public void getClassInstancesByCourseId(String courseId, ValueEventListener listener) {
        fetchClassSessionsByCourseId(courseId, listener);
    }

    public void updateClassInstance(YogaClassSession session, DatabaseReference.CompletionListener listener) {
        updateExistingClassSession(session, listener);
    }

    public void deleteClassInstance(String sessionId, DatabaseReference.CompletionListener listener) {
        removeClassSession(sessionId, listener);
    }

    public void deleteClassInstancesByCourseId(String courseId, DatabaseReference.CompletionListener listener) {
        removeAllClassSessionsByCourseId(courseId, listener);
    }

    /**
     * Delete all data from Firebase (courses and sessions)
     */
    public void deleteAllData(DatabaseReference.CompletionListener completionListener) {
        // First delete all sessions
        classSessionsReference.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                if (error != null) {
                    System.err.println("Error deleting all sessions: " + error.getMessage());
                }
                
                // Then delete all courses
                coursesReference.removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError error, DatabaseReference ref) {
                        if (completionListener != null) {
                            completionListener.onComplete(error, ref);
                        }
                    }
                });
            }
        });
    }
} 