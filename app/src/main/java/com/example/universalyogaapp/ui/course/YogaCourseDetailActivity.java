package com.example.universalyogaapp.ui.course;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.universalyogaapp.R;
import com.example.universalyogaapp.firebase.YogaFirebaseManager;
import com.example.universalyogaapp.model.YogaCourse;
import com.example.universalyogaapp.utils.YogaDateUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;
import androidx.room.Room;
import com.example.universalyogaapp.db.YogaAppDatabase;
import com.example.universalyogaapp.dao.YogaCourseDao;
import com.example.universalyogaapp.db.YogaCourseEntity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.universalyogaapp.model.YogaClassSession;
import java.util.ArrayList;
import java.util.List;
import com.example.universalyogaapp.ui.course.YogaClassSessionAdapter;
import com.example.universalyogaapp.dao.YogaClassSessionDao;
import com.example.universalyogaapp.db.YogaClassSessionEntity;

public class YogaCourseDetailActivity extends AppCompatActivity {
    // Constants
    private static final int REQUEST_EDIT_COURSE = 2001;
    
    // UI Components
    private TextView textViewName, textViewSchedule, textViewTime, textViewTeacher, textViewCapacity, textViewPrice, textViewDuration, textViewDescription, textViewNote;
    private Button buttonEdit, buttonDelete;
    private RecyclerView recyclerViewClassSessions;
    private YogaClassSessionAdapter classSessionAdapter;
    private Button buttonAddClassSession;

    // Business logic components
    private YogaCourse course;
    private YogaFirebaseManager firebaseManager;
    private String courseId;
    private YogaAppDatabase db;
    private List<YogaClassSession> classSessionList = new ArrayList<>();
    private String courseSchedule; // Store course schedule to pass to add/edit screen

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga_course_detail);

        initializeDatabase();
        initializeUserInterface();
        setupEventListeners();
        loadCourseData();
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
                .build();
    }

    /**
     * Initialize UI components
     */
    private void initializeUserInterface() {
        textViewName = findViewById(R.id.textViewName);
        textViewSchedule = findViewById(R.id.textViewSchedule);
        textViewTime = findViewById(R.id.textViewTime);
        textViewTeacher = findViewById(R.id.textViewTeacher);
        textViewCapacity = findViewById(R.id.textViewCapacity);
        textViewPrice = findViewById(R.id.textViewPrice);
        textViewDuration = findViewById(R.id.textViewDuration);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewNote = findViewById(R.id.textViewNote);
        buttonEdit = findViewById(R.id.buttonEdit);
        buttonDelete = findViewById(R.id.buttonDelete);
        recyclerViewClassSessions = findViewById(R.id.recyclerViewClassInstances);
        buttonAddClassSession = findViewById(R.id.buttonAddClassInstance);

        firebaseManager = new YogaFirebaseManager();
    }

    /**
     * Setup event listeners
     */
    private void setupEventListeners() {
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(YogaCourseDetailActivity.this, YogaCourseEditorActivity.class);
                intent.putExtra("course_id", courseId);
                startActivityForResult(intent, REQUEST_EDIT_COURSE);
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });

        classSessionAdapter = new YogaClassSessionAdapter(classSessionList, new YogaClassSessionAdapter.OnSessionActionListener() {
            @Override
            public void onEdit(YogaClassSession session) {
                Intent intent = new Intent(YogaCourseDetailActivity.this, YogaClassSessionEditorActivity.class);
                intent.putExtra("course_id", courseId);
                intent.putExtra("course_schedule", courseSchedule);
                intent.putExtra("class_session", session);
                startActivity(intent);
            }
            @Override
            public void onDelete(YogaClassSession session) {
                confirmDeleteSession(session);
            }
        });

        recyclerViewClassSessions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewClassSessions.setAdapter(classSessionAdapter);

        buttonAddClassSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(YogaCourseDetailActivity.this, YogaClassSessionEditorActivity.class);
                intent.putExtra("course_id", courseId);
                intent.putExtra("course_schedule", courseSchedule);
                startActivity(intent);
            }
        });
    }

    /**
     * Load course data
     */
    private void loadCourseData() {
        courseId = getIntent().getStringExtra("course_id");
        if (courseId != null) {
            loadCourse(courseId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (course != null) {
            loadClassSessions(course.getId());
        } else if (courseId != null) {
            loadClassSessions(courseId);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_EDIT_COURSE && resultCode == RESULT_OK) {
            // Course was edited, refresh the course data
            if (courseId != null) {
                loadCourse(courseId);
            }
        }
    }

    /**
     * Load course from Firebase
     */
    private void loadCourse(String id) {
        firebaseManager.fetchCourseById(id, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer capacityObj = snapshot.child("capacity").getValue(Integer.class);
                int capacity = capacityObj != null ? capacityObj : 0;
                Double priceObj = snapshot.child("price").getValue(Double.class);
                double price = priceObj != null ? priceObj : 0.0;
                Integer durationObj = snapshot.child("duration").getValue(Integer.class);
                int duration = durationObj != null ? durationObj : 0;
                
                YogaCourse course = new YogaCourse(
                        snapshot.getKey(),
                        snapshot.child("name").getValue(String.class),
                        snapshot.child("schedule").getValue(String.class),
                        snapshot.child("time").getValue(String.class),
                        snapshot.child("teacher").getValue(String.class),
                        capacity,
                        price,
                        duration,
                        snapshot.child("description").getValue(String.class),
                        snapshot.child("note").getValue(String.class),
                        snapshot.child("upcomingDate").getValue(String.class),
                        0 // hoặc 0 nếu không có
                );
                if (course != null) {
                    course.setId(snapshot.getKey());
                    course.setLocalId(0); // When getting from Firebase, localId doesn't exist, set 0
                    showCourseInfo(course);
                    loadClassSessions(course.getId());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(YogaCourseDetailActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Load class sessions for course
     */
    private void loadClassSessions(String courseId) {
        classSessionList.clear();
        List<YogaClassSessionEntity> entities = db.classSessionDao().getSessionsForCourse(courseId);
        for (YogaClassSessionEntity entity : entities) {
            YogaClassSession session = new YogaClassSession(
                    entity.getCloudDatabaseId(),
                    entity.getParentCourseId(),
                    entity.getClassDate(),
                    entity.getAssignedInstructor(),
                    entity.getClassNotes(),
                    entity.getLocalDatabaseId()
            );
            classSessionList.add(session);
        }
        classSessionAdapter.updateSessionList(new ArrayList<>(classSessionList));
    }

    /**
     * Display course information
     */
    private void showCourseInfo(YogaCourse course) {
        textViewName.setText(course.getName());
        textViewDescription.setText(course.getDescription());
        textViewSchedule.setText(YogaDateUtils.calculateNextUpcomingDate(course.getSchedule()));
        textViewTime.setText(course.getTime() != null ? course.getTime() : "Not set");
        textViewTeacher.setText(course.getTeacher());
        textViewCapacity.setText(String.format(Locale.getDefault(), "%d Students", course.getCapacity()));
        textViewPrice.setText(String.format(Locale.US, "$%.2f", course.getPrice()));
        textViewDuration.setText(String.format(Locale.getDefault(), "%d min", course.getDuration()));

        if (course.getNote() != null && !course.getNote().isEmpty()) {
            textViewNote.setText(course.getNote());
            textViewNote.setVisibility(View.VISIBLE);
        } else {
            textViewNote.setVisibility(View.GONE);
        }
        // Store schedule to pass
        courseSchedule = course.getSchedule();
    }

    /**
     * Confirm course deletion
     */
    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Course")
                .setMessage("Are you sure you want to delete this course?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteCourse();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Delete course and related sessions
     */
    private void deleteCourse() {
        if (courseId == null) return;
        
        // Show loading indicator
        Toast.makeText(this, "Deleting course and sessions...", Toast.LENGTH_SHORT).show();
        
        // Delete ClassSessions on Firebase first
        firebaseManager.removeAllClassSessionsByCourseId(courseId, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                if (error != null) {
                    // Log error but continue with course deletion
                    System.err.println("Error deleting sessions: " + error.getMessage());
                }
                
                // After deleting sessions, delete Course
                firebaseManager.removeCourse(courseId, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError error, DatabaseReference ref) {
                        if (error == null) {
                            // Delete local data
                            try {
                                // Delete local course
                                db.courseDao().deleteByCloudId(courseId);
                                
                                // Delete all local sessions for this course
                                List<YogaClassSessionEntity> localSessions = db.classSessionDao().getSessionsForCourse(courseId);
                                for (YogaClassSessionEntity entity : localSessions) {
                                    db.classSessionDao().deleteClassSession(entity);
                                }
                                
                                runOnUiThread(() -> {
                                    Toast.makeText(YogaCourseDetailActivity.this, "Course and related class sessions deleted successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            } catch (Exception e) {
                                runOnUiThread(() -> {
                                    Toast.makeText(YogaCourseDetailActivity.this, "Error deleting local data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(YogaCourseDetailActivity.this, "Error deleting course from Firebase: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
            }
        });
    }

    /**
     * Confirm class session deletion
     */
    private void confirmDeleteSession(YogaClassSession session) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Class Session")
                .setMessage("Are you sure you want to delete this class session?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteClassSession(session);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Delete class session
     */
    private void deleteClassSession(YogaClassSession session) {
        firebaseManager.removeClassSession(session.getId(), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                if (error == null) {
                    // Delete from local database - try multiple approaches
                    YogaClassSessionEntity entity = db.classSessionDao().getSessionByCloudId(session.getId());
                    if (entity == null) {
                        // Try to find by local ID if cloud ID not found
                        // This might happen if the session was created locally but not synced yet
                        List<YogaClassSessionEntity> allSessions = db.classSessionDao().getSessionsForCourse(courseId);
                        for (YogaClassSessionEntity localEntity : allSessions) {
                            if (localEntity.getLocalDatabaseId() == session.getLocalId() || 
                                (session.getId() != null && session.getId().equals(localEntity.getCloudDatabaseId()))) {
                                entity = localEntity;
                                break;
                            }
                        }
                    }
                    
                    if (entity != null) {
                        db.classSessionDao().deleteClassSession(entity);
                    }
                    
                    // Refresh the list on UI thread
                    runOnUiThread(() -> {
                        Toast.makeText(YogaCourseDetailActivity.this, "Class session deleted successfully", Toast.LENGTH_SHORT).show();
                        // Reload class sessions to refresh the list
                        loadClassSessions(courseId);
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(YogaCourseDetailActivity.this, "Error deleting class session: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
} 