package com.example.universalyogaapp.ui.course;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.universalyogaapp.R;
import com.example.universalyogaapp.firebase.YogaFirebaseManager;
import com.example.universalyogaapp.model.YogaCourse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import androidx.room.Room;
import com.example.universalyogaapp.db.YogaAppDatabase;
import com.example.universalyogaapp.db.YogaCourseEntity;
import com.example.universalyogaapp.db.YogaClassSessionEntity;
import com.example.universalyogaapp.dao.YogaClassSessionDao;
import com.example.universalyogaapp.model.YogaClassSession;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class YogaCourseListActivity extends AppCompatActivity {
    // Constants
    private static final int REQUEST_ADD_EDIT_COURSE = 1001;
    
    // UI Components
    private RecyclerView recyclerView;
    private YogaCourseAdapter adapter;
    private List<YogaCourse> courseList;
    private List<YogaCourse> fullCourseList; // Store all for filtering
    private MaterialButton buttonSync;

    // Business logic components
    private YogaFirebaseManager firebaseManager;
    private YogaAppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga_course_list);

        initializeDatabase();
        initializeUserInterface();
        setupEventListeners();
        loadCourses();
    }

    /**
     * Initialize database
     */
    private void initializeDatabase() {
        db = Room.databaseBuilder(
                        getApplicationContext(),
                        YogaAppDatabase.class,
                        "yoga-db"
                ).allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
    }

    /**
     * Initialize UI components
     */
    private void initializeUserInterface() {
        recyclerView = findViewById(R.id.recyclerViewCourses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        courseList = new ArrayList<>();
        fullCourseList = new ArrayList<>();
        adapter = new YogaCourseAdapter();
        recyclerView.setAdapter(adapter);
        firebaseManager = new YogaFirebaseManager();
    }

    /**
     * Setup event listeners
     */
    private void setupEventListeners() {
        adapter.setOnItemClickListener(new YogaCourseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(YogaCourse course) {
                Intent intent = new Intent(YogaCourseListActivity.this, YogaCourseDetailActivity.class);
                intent.putExtra("course_id", course.getId());
                startActivityForResult(intent, REQUEST_ADD_EDIT_COURSE);
            }
        });

        MaterialButton buttonAddCourse = findViewById(R.id.buttonAddCourse);
        buttonAddCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(YogaCourseListActivity.this, YogaCourseEditorActivity.class);
                startActivityForResult(intent, REQUEST_ADD_EDIT_COURSE);
            }
        });

        TextInputEditText editTextSearch = findViewById(R.id.editTextSearch);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCourses(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        buttonSync = findViewById(R.id.buttonSync);
        buttonSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Disable button and show loading state
                buttonSync.setEnabled(false);
                buttonSync.setIcon(ContextCompat.getDrawable(YogaCourseListActivity.this, R.drawable.ic_sync));

                // Perform sync operations
                performDataSync();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Force cleanup and refresh the course list when returning to this activity
        forceCleanupAndRefresh();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_ADD_EDIT_COURSE && resultCode == RESULT_OK) {
            // Data was updated, force cleanup and refresh the list immediately
            forceCleanupAndRefresh();
        }
    }

    /**
     * Load courses from database
     */
    private void loadCourses() {
        // Clean up duplicates first
        cleanupDuplicateCourses();
        
        courseList.clear();
        fullCourseList.clear();
        List<YogaCourseEntity> entities = db.courseDao().getAllCourses();
        
        System.out.println("Loading courses from database: " + entities.size() + " total courses found");

        // Use a Set to track unique courses by name to avoid duplicates in display
        Set<String> addedCourseNames = new HashSet<>();

        for (YogaCourseEntity entity : entities) {
            String courseName = entity.getCourseName();
            
            // Debug log for sync status
            System.out.println("Course: " + courseName + " (Local ID: " + entity.getLocalDatabaseId() + ", Cloud ID: " + entity.getCloudDatabaseId() + ", Sync Status: " + entity.getCloudSyncStatus() + ")");
            
            // Only add if we haven't seen this course name before
            if (courseName != null && !addedCourseNames.contains(courseName)) {
                YogaCourse course = new YogaCourse(
                        entity.getCloudDatabaseId(),
                        entity.getCourseName(),
                        entity.getWeeklySchedule(),
                        entity.getClassTime(),
                        entity.getInstructorName(),
                        entity.getMaxStudents(),
                        entity.getCoursePrice(),
                        entity.getSessionDuration(),
                        entity.getCourseDescription(),
                        entity.getAdditionalNotes(),
                        entity.getNextClassDate(),
                        entity.getLocalDatabaseId()
                );
                courseList.add(course);
                fullCourseList.add(course);
                addedCourseNames.add(courseName);
                
                System.out.println("Added course to display: " + courseName);
            } else if (courseName != null) {
                System.out.println("Skipped duplicate course: " + courseName);
            }
        }

        adapter.updateCourseList(courseList);
        
        // Show sync status summary
        long unsyncedCount = entities.stream().filter(e -> !e.getCloudSyncStatus()).count();
        System.out.println("Loaded " + courseList.size() + " unique courses for display. " + unsyncedCount + " courses need sync.");
    }

    /**
     * Force refresh the course list with proper notification
     */
    private void refreshCourseList() {
        // Clean up any duplicate courses first
        cleanupDuplicateCourses();
        
        // Clear existing data
        courseList.clear();
        fullCourseList.clear();
        
        // Load fresh data from database
        List<YogaCourseEntity> entities = db.courseDao().getAllCourses();

        for (YogaCourseEntity entity : entities) {
            YogaCourse course = new YogaCourse(
                    entity.getCloudDatabaseId(),
                    entity.getCourseName(),
                    entity.getWeeklySchedule(),
                    entity.getClassTime(),
                    entity.getInstructorName(),
                    entity.getMaxStudents(),
                    entity.getCoursePrice(),
                    entity.getSessionDuration(),
                    entity.getCourseDescription(),
                    entity.getAdditionalNotes(),
                    entity.getNextClassDate(),
                    entity.getLocalDatabaseId()
            );
            courseList.add(course);
            fullCourseList.add(course);
        }

        // Force adapter to refresh with new data
        adapter.updateCourseList(new ArrayList<>(courseList));
        
        // Log for debugging
        System.out.println("Refreshed course list with " + courseList.size() + " courses");
        
        // Also sync from Firebase to ensure data consistency
        syncFromFirebase();
    }

    /**
     * Clean up duplicate courses based on name
     */
    private void cleanupDuplicateCourses() {
        List<YogaCourseEntity> allCourses = db.courseDao().getAllCourses();
        Map<String, List<YogaCourseEntity>> coursesByName = new HashMap<>();
        
        System.out.println("Starting cleanup of " + allCourses.size() + " total courses");
        
        // Group courses by name
        for (YogaCourseEntity course : allCourses) {
            String name = course.getCourseName();
            if (name != null) {
                coursesByName.computeIfAbsent(name, k -> new ArrayList<>()).add(course);
            }
        }
        
        int totalDeleted = 0;
        
        // Remove duplicates, keeping the one with Firebase ID if available
        for (Map.Entry<String, List<YogaCourseEntity>> entry : coursesByName.entrySet()) {
            List<YogaCourseEntity> duplicates = entry.getValue();
            if (duplicates.size() > 1) {
                System.out.println("Found " + duplicates.size() + " duplicates for course: " + entry.getKey());
                
                // Find the course with Firebase ID (preferred)
                YogaCourseEntity courseToKeep = null;
                for (YogaCourseEntity course : duplicates) {
                    if (course.getCloudDatabaseId() != null && !course.getCloudDatabaseId().isEmpty()) {
                        courseToKeep = course;
                        break;
                    }
                }
                
                // If no course with Firebase ID, keep the one with the highest local ID (most recent)
                if (courseToKeep == null) {
                    courseToKeep = duplicates.get(0);
                    for (YogaCourseEntity course : duplicates) {
                        if (course.getLocalDatabaseId() > courseToKeep.getLocalDatabaseId()) {
                            courseToKeep = course;
                        }
                    }
                }
                
                // Delete the rest
                for (YogaCourseEntity course : duplicates) {
                    if (course.getLocalDatabaseId() != courseToKeep.getLocalDatabaseId()) {
                        db.courseDao().deleteCourse(course);
                        totalDeleted++;
                        System.out.println("Deleted duplicate course: " + course.getCourseName() + " (ID: " + course.getLocalDatabaseId() + ")");
                    }
                }
            }
        }
        
        System.out.println("Cleanup completed. Deleted " + totalDeleted + " duplicate courses");
    }

    /**
     * Force cleanup and refresh course list
     */
    private void forceCleanupAndRefresh() {
        // Debug: Show current database state
        debugDatabaseState();
        
        // First, clean up any duplicates in the database
        cleanupDuplicateCourses();
        
        // Then load courses from database (which will skip duplicates in display)
        loadCourses();
        
        // Finally, sync from Firebase to ensure data consistency
        syncFromFirebase();
    }

    /**
     * Debug method to show current database state
     */
    private void debugDatabaseState() {
        List<YogaCourseEntity> allCourses = db.courseDao().getAllCourses();
        System.out.println("=== DATABASE DEBUG ===");
        System.out.println("Total courses in database: " + allCourses.size());
        
        Map<String, List<YogaCourseEntity>> coursesByName = new HashMap<>();
        for (YogaCourseEntity course : allCourses) {
            String name = course.getCourseName();
            if (name != null) {
                coursesByName.computeIfAbsent(name, k -> new ArrayList<>()).add(course);
            }
        }
        
        for (Map.Entry<String, List<YogaCourseEntity>> entry : coursesByName.entrySet()) {
            List<YogaCourseEntity> courses = entry.getValue();
            if (courses.size() > 1) {
                System.out.println("DUPLICATE FOUND: " + entry.getKey() + " (" + courses.size() + " instances)");
                for (YogaCourseEntity course : courses) {
                    System.out.println("  - ID: " + course.getLocalDatabaseId() + 
                                     ", Firebase ID: " + course.getCloudDatabaseId() + 
                                     ", Name: " + course.getCourseName() +
                                     ", Sync Status: " + course.getCloudSyncStatus());
                }
            } else {
                YogaCourseEntity course = courses.get(0);
                System.out.println("COURSE: " + entry.getKey() + 
                                 " (Local ID: " + course.getLocalDatabaseId() + 
                                 ", Cloud ID: " + course.getCloudDatabaseId() + 
                                 ", Sync Status: " + course.getCloudSyncStatus() + ")");
            }
        }
        
        // Show unsynced courses specifically
        List<YogaCourseEntity> unsyncedCourses = db.courseDao().getUnsyncedCourses();
        System.out.println("UNSYNCED COURSES: " + unsyncedCourses.size());
        for (YogaCourseEntity course : unsyncedCourses) {
            System.out.println("  - UNSYNCED: " + course.getCourseName() + 
                             " (Local ID: " + course.getLocalDatabaseId() + 
                             ", Cloud ID: " + course.getCloudDatabaseId() + ")");
        }
        
        System.out.println("=== END DEBUG ===");
    }

    /**
     * Force refresh course list with immediate sync from Firebase
     */
    private void forceRefreshCourseList() {
        // First, sync from Firebase to ensure we have the latest data
        firebaseManager.fetchAllCourses(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final boolean[] hasChanges = {false};
                
                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    String firebaseId = courseSnapshot.getKey();
                    String name = courseSnapshot.child("name").getValue(String.class);
                    String schedule = courseSnapshot.child("schedule").getValue(String.class);
                    String time = courseSnapshot.child("time").getValue(String.class);
                    String teacher = courseSnapshot.child("teacher").getValue(String.class);
                    Integer capacity = courseSnapshot.child("capacity").getValue(Integer.class);
                    Double price = courseSnapshot.child("price").getValue(Double.class);
                    Integer duration = courseSnapshot.child("duration").getValue(Integer.class);
                    String description = courseSnapshot.child("description").getValue(String.class);
                    String note = courseSnapshot.child("note").getValue(String.class);
                    String upcomingDate = courseSnapshot.child("upcomingDate").getValue(String.class);

                    if (name != null) {
                        // Check if course exists in local database
                        YogaCourseEntity existingEntity = db.courseDao().getCourseByCloudId(firebaseId);
                        
                        if (existingEntity != null) {
                            // Update existing course if data is different
                            boolean needsUpdate = !name.equals(existingEntity.getCourseName()) ||
                                    !schedule.equals(existingEntity.getWeeklySchedule()) ||
                                    !time.equals(existingEntity.getClassTime()) ||
                                    !teacher.equals(existingEntity.getInstructorName());
                            
                            if (needsUpdate) {
                                existingEntity.setCourseName(name);
                                existingEntity.setWeeklySchedule(schedule);
                                existingEntity.setClassTime(time);
                                existingEntity.setInstructorName(teacher);
                                existingEntity.setMaxStudents(capacity != null ? capacity : 0);
                                existingEntity.setCoursePrice(price != null ? price : 0.0);
                                existingEntity.setSessionDuration(duration != null ? duration : 0);
                                existingEntity.setCourseDescription(description);
                                existingEntity.setAdditionalNotes(note);
                                existingEntity.setNextClassDate(upcomingDate);
                                existingEntity.setCloudSyncStatus(true);
                                
                                db.courseDao().updateCourse(existingEntity);
                                hasChanges[0] = true;
                                System.out.println("Updated existing course: " + name);
                            }
                        } else {
                            // Check if there's a course with the same name to avoid duplicates
                            List<YogaCourseEntity> coursesWithSameName = db.courseDao().getCoursesByName(name);
                            if (coursesWithSameName.isEmpty()) {
                                // Insert new course only if no course with same name exists
                                YogaCourseEntity newEntity = new YogaCourseEntity();
                                newEntity.setCloudDatabaseId(firebaseId);
                                newEntity.setCourseName(name);
                                newEntity.setWeeklySchedule(schedule);
                                newEntity.setClassTime(time);
                                newEntity.setInstructorName(teacher);
                                newEntity.setMaxStudents(capacity != null ? capacity : 0);
                                newEntity.setCoursePrice(price != null ? price : 0.0);
                                newEntity.setSessionDuration(duration != null ? duration : 0);
                                newEntity.setCourseDescription(description);
                                newEntity.setAdditionalNotes(note);
                                newEntity.setNextClassDate(upcomingDate);
                                newEntity.setCloudSyncStatus(true);
                                
                                db.courseDao().insertCourse(newEntity);
                                hasChanges[0] = true;
                                System.out.println("Inserted new course: " + name);
                            } else {
                                // Update the existing course with same name to have the Firebase ID
                                YogaCourseEntity existingCourse = coursesWithSameName.get(0);
                                existingCourse.setCloudDatabaseId(firebaseId);
                                existingCourse.setCloudSyncStatus(true);
                                db.courseDao().updateCourse(existingCourse);
                                hasChanges[0] = true;
                                System.out.println("Updated existing course with Firebase ID: " + name);
                            }
                        }
                    }
                }
                
                // Refresh the list after syncing
                runOnUiThread(() -> {
                    loadCourses();
                    if (hasChanges[0]) {
                        System.out.println("Force refreshed course list with changes from Firebase");
                    } else {
                        System.out.println("Force refreshed course list - no changes detected");
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Failed to force refresh from Firebase: " + error.getMessage());
                // Fallback to local database
                runOnUiThread(() -> {
                    loadCourses();
                });
            }
        });
    }

    /**
     * Sync data from Firebase to local database
     */
    private void syncFromFirebase() {
        firebaseManager.fetchAllCourses(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    String firebaseId = courseSnapshot.getKey();
                    String name = courseSnapshot.child("name").getValue(String.class);
                    String schedule = courseSnapshot.child("schedule").getValue(String.class);
                    String time = courseSnapshot.child("time").getValue(String.class);
                    String teacher = courseSnapshot.child("teacher").getValue(String.class);
                    Integer capacity = courseSnapshot.child("capacity").getValue(Integer.class);
                    Double price = courseSnapshot.child("price").getValue(Double.class);
                    Integer duration = courseSnapshot.child("duration").getValue(Integer.class);
                    String description = courseSnapshot.child("description").getValue(String.class);
                    String note = courseSnapshot.child("note").getValue(String.class);
                    String upcomingDate = courseSnapshot.child("upcomingDate").getValue(String.class);

                    if (name != null) {
                        // Check if course exists in local database
                        YogaCourseEntity existingEntity = db.courseDao().getCourseByCloudId(firebaseId);
                        
                        if (existingEntity != null) {
                            // Check if local course has unsynced changes
                            if (existingEntity.getCloudSyncStatus()) {
                                // Local course is synced, safe to update from Firebase
                                existingEntity.setCourseName(name);
                                existingEntity.setWeeklySchedule(schedule);
                                existingEntity.setClassTime(time);
                                existingEntity.setInstructorName(teacher);
                                existingEntity.setMaxStudents(capacity != null ? capacity : 0);
                                existingEntity.setCoursePrice(price != null ? price : 0.0);
                                existingEntity.setSessionDuration(duration != null ? duration : 0);
                                existingEntity.setCourseDescription(description);
                                existingEntity.setAdditionalNotes(note);
                                existingEntity.setNextClassDate(upcomingDate);
                                existingEntity.setCloudSyncStatus(true);
                                
                                db.courseDao().updateCourse(existingEntity);
                            }
                        } else {
                            // Check if there's a course with the same name to avoid duplicates
                            List<YogaCourseEntity> coursesWithSameName = db.courseDao().getCoursesByName(name);
                            if (coursesWithSameName.isEmpty()) {
                                // Insert new course only if no course with same name exists
                                YogaCourseEntity newEntity = new YogaCourseEntity();
                                newEntity.setCloudDatabaseId(firebaseId);
                                newEntity.setCourseName(name);
                                newEntity.setWeeklySchedule(schedule);
                                newEntity.setClassTime(time);
                                newEntity.setInstructorName(teacher);
                                newEntity.setMaxStudents(capacity != null ? capacity : 0);
                                newEntity.setCoursePrice(price != null ? price : 0.0);
                                newEntity.setSessionDuration(duration != null ? duration : 0);
                                newEntity.setCourseDescription(description);
                                newEntity.setAdditionalNotes(note);
                                newEntity.setNextClassDate(upcomingDate);
                                newEntity.setCloudSyncStatus(true);
                                
                                db.courseDao().insertCourse(newEntity);
                            } else {
                                // Update the existing course with same name to have the Firebase ID
                                YogaCourseEntity existingCourse = coursesWithSameName.get(0);
                                existingCourse.setCloudDatabaseId(firebaseId);
                                existingCourse.setCloudSyncStatus(true);
                                db.courseDao().updateCourse(existingCourse);
                            }
                        }
                    }
                }
                
                // Refresh the list after syncing
                runOnUiThread(() -> {
                    loadCourses();
                    System.out.println("Synced from Firebase and refreshed course list");
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Failed to sync from Firebase: " + error.getMessage());
            }
        });
    }

    /**
     * Filter courses based on keyword
     */
    private void filterCourses(String keyword) {
        List<YogaCourse> filtered = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        SimpleDateFormat[] dateFormats = new SimpleDateFormat[] {
                new SimpleDateFormat("yyyy-MM-dd", Locale.US),
                new SimpleDateFormat("dd/MM/yyyy", Locale.US)
        };
        String dayOfWeek = null;

        // Try to parse date
        for (SimpleDateFormat sdf : dateFormats) {
            try {
                Date date = sdf.parse(keyword);
                if (date != null) {
                    String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime(date);
                    dayOfWeek = days[cal.get(java.util.Calendar.DAY_OF_WEEK) - 1];
                }
            } catch (ParseException ignored) {}
        }

        for (YogaCourse course : fullCourseList) {
            boolean match = false;
            // If input is a date, filter by day of week
            if (dayOfWeek != null) {
                match = course.getSchedule() != null && course.getSchedule().toLowerCase().contains(dayOfWeek.toLowerCase());
            } else {
                // If input is day name
                String[] weekDays = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday", "mon", "tue", "wed", "thu", "fri", "sat", "sun"};
                for (String wd : weekDays) {
                    if (lowerKeyword.equals(wd) && course.getSchedule() != null && course.getSchedule().toLowerCase().contains(wd)) {
                        match = true;
                        break;
                    }
                }
                // If input is teacher name
                if (!match && course.getTeacher() != null && course.getTeacher().toLowerCase().contains(lowerKeyword)) {
                    match = true;
                }
                // If input is course name or schedule
                if (!match && course.getName() != null && course.getName().toLowerCase().contains(lowerKeyword)) {
                    match = true;
                }
                if (!match && course.getSchedule() != null && course.getSchedule().toLowerCase().contains(lowerKeyword)) {
                    match = true;
                }
            }
            if (match) {
                filtered.add(course);
            }
        }
        adapter.updateCourseList(filtered);
    }

    /**
     * Sync courses to Firebase
     */
    public void syncCoursesToFirebase() {
        List<YogaCourseEntity> unsynced = db.courseDao().getUnsyncedCourses();
        YogaFirebaseManager firebaseManager = new YogaFirebaseManager();

        System.out.println("Starting sync: Found " + unsynced.size() + " unsynced courses");

        for (YogaCourseEntity entity : unsynced) {
            System.out.println("Syncing course: " + entity.getCourseName() + " (Local ID: " + entity.getLocalDatabaseId() + ", Cloud ID: " + entity.getCloudDatabaseId() + ", Sync Status: " + entity.getCloudSyncStatus() + ")");
            
            YogaCourse course = new YogaCourse(
                    entity.getCloudDatabaseId(), entity.getCourseName(), entity.getWeeklySchedule(), entity.getClassTime(), entity.getInstructorName(),
                    entity.getMaxStudents(), entity.getCoursePrice(), entity.getSessionDuration(), entity.getCourseDescription(), entity.getAdditionalNotes(), entity.getNextClassDate(), entity.getLocalDatabaseId()
            );

            if (entity.getCloudDatabaseId() != null && !entity.getCloudDatabaseId().isEmpty()) {
                // Course already exists in Firebase, update it
                course.setId(entity.getCloudDatabaseId());
                System.out.println("Updating existing course in Firebase: " + course.getCourseName() + " with ID: " + course.getId());
                firebaseManager.updateExistingCourse(course, (error, ref) -> {
                    if (error == null) {
                        System.out.println("Successfully updated course in Firebase: " + course.getCourseName());
                        // Update the entity in a new transaction to ensure changes are persisted
                        runOnUiThread(() -> {
                            try {
                                YogaCourseEntity updatedEntity = db.courseDao().getCourseByCloudId(entity.getCloudDatabaseId());
                                if (updatedEntity != null) {
                                    updatedEntity.setCloudSyncStatus(true);
                                    db.courseDao().updateCourse(updatedEntity);
                                    System.out.println("Marked course as synced in local database: " + updatedEntity.getCourseName());
                                }
                            } catch (Exception e) {
                                System.err.println("Error updating local sync status: " + e.getMessage());
                            }
                        });
                    } else {
                        System.err.println("Failed to update course in Firebase: " + error.getMessage());
                    }
                });
            } else {
                // Course doesn't exist in Firebase, create new one
                course.setId(null);
                System.out.println("Creating new course in Firebase: " + course.getCourseName());
                firebaseManager.createNewCourse(course, (error, ref) -> {
                    if (error == null && ref != null) {
                        System.out.println("Successfully created course in Firebase: " + course.getCourseName() + " with new ID: " + ref.getKey());
                        // Update the entity in a new transaction to ensure changes are persisted
                        runOnUiThread(() -> {
                            try {
                                YogaCourseEntity updatedEntity = db.courseDao().getAllCourses().stream()
                                        .filter(e -> e.getLocalDatabaseId() == entity.getLocalDatabaseId())
                                        .findFirst().orElse(null);
                                if (updatedEntity != null) {
                                    updatedEntity.setCloudSyncStatus(true);
                                    updatedEntity.setCloudDatabaseId(ref.getKey());
                                    db.courseDao().updateCourse(updatedEntity);
                                    System.out.println("Marked new course as synced in local database: " + updatedEntity.getCourseName());
                                }
                            } catch (Exception e) {
                                System.err.println("Error updating local sync status for new course: " + e.getMessage());
                            }
                        });
                    } else {
                        System.err.println("Failed to create course in Firebase: " + (error != null ? error.getMessage() : "Unknown error"));
                    }
                });
            }
        }
    }

    /**
     * Sync class sessions to Firebase
     */
    public void syncClassSessionsToFirebase() {
        List<YogaClassSessionEntity> unsynced = db.classSessionDao().getUnsyncedSessions();
        YogaFirebaseManager firebaseManager = new YogaFirebaseManager();

        System.out.println("Starting session sync: Found " + unsynced.size() + " unsynced sessions");

        for (YogaClassSessionEntity entity : unsynced) {
            System.out.println("Syncing session: " + entity.getClassDate() + " (Local ID: " + entity.getLocalDatabaseId() + ", Cloud ID: " + entity.getCloudDatabaseId() + ", Deleted: " + entity.getIsDeleted() + ")");
            
            if (entity.getIsDeleted()) {
                // This session was marked for deletion - delete from Firebase
                if (entity.getCloudDatabaseId() != null && !entity.getCloudDatabaseId().isEmpty()) {
                    firebaseManager.removeClassSession(entity.getCloudDatabaseId(), (error, ref) -> {
                        if (error == null) {
                            System.out.println("Successfully deleted session from Firebase: " + entity.getClassDate());
                            // Now delete from local database
                            runOnUiThread(() -> {
                                try {
                                    db.classSessionDao().deleteClassSession(entity);
                                    System.out.println("Deleted session from local database after Firebase sync");
                                } catch (Exception e) {
                                    System.err.println("Error deleting local session after Firebase sync: " + e.getMessage());
                                }
                            });
                        } else {
                            System.err.println("Failed to delete session from Firebase: " + error.getMessage());
                        }
                    });
                } else {
                    // No cloud ID, just delete from local
                    db.classSessionDao().deleteClassSession(entity);
                    System.out.println("Deleted local-only session: " + entity.getClassDate());
                }
            } else {
                // Normal session sync (create/update)
                YogaClassSession session = new YogaClassSession(
                        null, entity.getCloudDatabaseId() != null ? entity.getCloudDatabaseId() : String.valueOf(entity.getParentCourseId()),
                        entity.getClassDate(), entity.getAssignedInstructor(), entity.getClassNotes(), entity.getLocalDatabaseId()
                );
                firebaseManager.createNewClassSession(session, (error, ref) -> {
                    if (error == null && ref != null) {
                        System.out.println("Successfully synced session: " + session.getDate());
                        // Update the entity in a new transaction to ensure changes are persisted
                        runOnUiThread(() -> {
                            try {
                                YogaClassSessionEntity updatedEntity = db.classSessionDao().getAllSessions().stream()
                                        .filter(e -> e.getLocalDatabaseId() == entity.getLocalDatabaseId())
                                        .findFirst().orElse(null);
                                if (updatedEntity != null) {
                                    updatedEntity.setCloudSyncStatus(true);
                                    updatedEntity.setCloudDatabaseId(ref.getKey());
                                    db.classSessionDao().updateClassSession(updatedEntity);
                                    System.out.println("Marked session as synced in local database");
                                }
                            } catch (Exception e) {
                                System.err.println("Error updating local session sync status: " + e.getMessage());
                            }
                        });
                    } else {
                        System.err.println("Failed to sync session: " + (error != null ? error.getMessage() : "Unknown error"));
                    }
                });
            }
        }
    }

    /**
     * Perform data synchronization with visual feedback
     */
    private void performDataSync() {
        Toast.makeText(YogaCourseListActivity.this, "Syncing data...", Toast.LENGTH_SHORT).show();

        // Debug: Show database state before sync
        System.out.println("=== BEFORE SYNC ===");
        debugDatabaseState();

        // Get unsynced data counts
        List<YogaCourseEntity> unsyncedCourses = db.courseDao().getUnsyncedCourses();
        List<YogaClassSessionEntity> unsyncedSessions = db.classSessionDao().getUnsyncedSessions();

        int totalUnsynced = unsyncedCourses.size() + unsyncedSessions.size();
        
        System.out.println("Sync started - Unsynced courses: " + unsyncedCourses.size() + ", Unsynced sessions: " + unsyncedSessions.size());

        if (totalUnsynced == 0) {
            Toast.makeText(YogaCourseListActivity.this, "All data is already synced!", Toast.LENGTH_SHORT).show();
            buttonSync.setEnabled(true);
            return;
        }

        // Perform sync operations
        syncCoursesToFirebase();
        syncClassSessionsToFirebase();

        // Wait longer for sync to complete and refresh the data
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            buttonSync.setEnabled(true);
            
            // Force refresh after sync
            loadCourses();
            
            // Debug: Show database state after sync
            System.out.println("=== AFTER SYNC ===");
            debugDatabaseState();
            
            // Check if sync was successful
            List<YogaCourseEntity> stillUnsynced = db.courseDao().getUnsyncedCourses();
            List<YogaClassSessionEntity> stillUnsyncedSessions = db.classSessionDao().getUnsyncedSessions();
            int remainingUnsynced = stillUnsynced.size() + stillUnsyncedSessions.size();
            
            if (remainingUnsynced == 0) {
                Toast.makeText(YogaCourseListActivity.this, "Sync completed successfully!", Toast.LENGTH_SHORT).show();
                System.out.println("Sync completed - All data synced successfully");
            } else {
                Toast.makeText(YogaCourseListActivity.this, "Sync completed with " + remainingUnsynced + " items still pending", Toast.LENGTH_SHORT).show();
                System.out.println("Sync completed but " + remainingUnsynced + " items still unsynced");
                
                // Show details of remaining unsynced items
                for (YogaCourseEntity course : stillUnsynced) {
                    System.out.println("  - Still unsynced course: " + course.getCourseName() + " (Local ID: " + course.getLocalDatabaseId() + ", Cloud ID: " + course.getCloudDatabaseId() + ")");
                }
            }
        }, 5000); // Wait 5 seconds instead of 2 to ensure all async operations complete
    }
} 