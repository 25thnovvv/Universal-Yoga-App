package com.example.universalyogaapp.firebase;

import androidx.annotation.NonNull;

import com.example.universalyogaapp.model.Course;
import com.example.universalyogaapp.model.ClassInstance;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseManager {
    // Firebase configuration
    private static final String FIREBASE_DATABASE_URL = "https://universayogaapp-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private final FirebaseDatabase firebaseDatabase;
    private final DatabaseReference coursesReference;
    private final DatabaseReference classInstancesReference;

    // Constructor
    public FirebaseManager() {
        firebaseDatabase = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL);
        coursesReference = firebaseDatabase.getReference("courses");
        classInstancesReference = firebaseDatabase.getReference("class_instances");
    }

    // Course management methods
    public void createNewCourse(Course course, DatabaseReference.CompletionListener completionListener) {
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

    public void updateExistingCourse(Course course, DatabaseReference.CompletionListener completionListener) {
        if (course.getId() == null) return;
        coursesReference.child(course.getId()).setValue(course, completionListener);
    }

    public void removeCourse(String courseId, DatabaseReference.CompletionListener completionListener) {
        coursesReference.child(courseId).removeValue(completionListener);
    }

    // Class instance management methods
    public void createNewClassInstance(ClassInstance classInstance, DatabaseReference.CompletionListener completionListener) {
        String generatedId = classInstancesReference.push().getKey();
        classInstance.setId(generatedId);
        classInstancesReference.child(generatedId).setValue(classInstance, completionListener);
    }

    public void fetchClassInstancesByCourseId(String courseId, ValueEventListener valueEventListener) {
        classInstancesReference.orderByChild("courseId").equalTo(courseId).addValueEventListener(valueEventListener);
    }

    public void updateExistingClassInstance(ClassInstance classInstance, DatabaseReference.CompletionListener completionListener) {
        if (classInstance.getId() == null) return;
        classInstancesReference.child(classInstance.getId()).setValue(classInstance, completionListener);
    }

    public void removeClassInstance(String instanceId, DatabaseReference.CompletionListener completionListener) {
        classInstancesReference.child(instanceId).removeValue(completionListener);
    }

    public void removeAllClassInstancesByCourseId(String courseId, DatabaseReference.CompletionListener completionListener) {
        classInstancesReference.orderByChild("courseId").equalTo(courseId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    childSnapshot.getRef().removeValue();
                }
                if (completionListener != null) completionListener.onComplete(null, classInstancesReference);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (completionListener != null) completionListener.onComplete(databaseError, classInstancesReference);
            }
        });
    }

    // Legacy methods for backward compatibility
    public void addCourse(Course course, DatabaseReference.CompletionListener listener) {
        createNewCourse(course, listener);
    }

    public void getCourses(ValueEventListener listener) {
        fetchAllCourses(listener);
    }

    public void getCourseById(String courseId, ValueEventListener listener) {
        fetchCourseById(courseId, listener);
    }

    public void updateCourse(Course course, DatabaseReference.CompletionListener listener) {
        updateExistingCourse(course, listener);
    }

    public void deleteCourse(String courseId, DatabaseReference.CompletionListener listener) {
        removeCourse(courseId, listener);
    }

    public void addClassInstance(ClassInstance instance, DatabaseReference.CompletionListener listener) {
        createNewClassInstance(instance, listener);
    }

    public void getClassInstancesByCourseId(String courseId, ValueEventListener listener) {
        fetchClassInstancesByCourseId(courseId, listener);
    }

    public void updateClassInstance(ClassInstance instance, DatabaseReference.CompletionListener listener) {
        updateExistingClassInstance(instance, listener);
    }

    public void deleteClassInstance(String instanceId, DatabaseReference.CompletionListener listener) {
        removeClassInstance(instanceId, listener);
    }

    public void deleteClassInstancesByCourseId(String courseId, DatabaseReference.CompletionListener listener) {
        removeAllClassInstancesByCourseId(courseId, listener);
    }
} 