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
                startActivity(intent);
            }
        });

        MaterialButton buttonAddCourse = findViewById(R.id.buttonAddCourse);
        buttonAddCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(YogaCourseListActivity.this, YogaCourseEditorActivity.class);
                startActivity(intent);
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
        loadCourses();
    }

    /**
     * Load courses from database
     */
    private void loadCourses() {
        courseList.clear();
        fullCourseList.clear();
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

        adapter.updateCourseList(courseList);
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

        for (YogaCourseEntity entity : unsynced) {
            YogaCourse course = new YogaCourse(
                    null, entity.getCourseName(), entity.getWeeklySchedule(), entity.getClassTime(), entity.getInstructorName(),
                    entity.getMaxStudents(), entity.getCoursePrice(), entity.getSessionDuration(), entity.getCourseDescription(), entity.getAdditionalNotes(), entity.getNextClassDate(), entity.getLocalDatabaseId()
            );
            firebaseManager.createNewCourse(course, (error, ref) -> {
                if (error == null) {
                    entity.setCloudSyncStatus(true);
                    entity.setCloudDatabaseId(ref.getKey());
                    db.courseDao().updateCourse(entity);
                }
            });
        }
    }

    /**
     * Sync class sessions to Firebase
     */
    public void syncClassSessionsToFirebase() {
        List<YogaClassSessionEntity> unsynced = db.classSessionDao().getUnsyncedSessions();
        YogaFirebaseManager firebaseManager = new YogaFirebaseManager();

        for (YogaClassSessionEntity entity : unsynced) {
            // Note: entity.parentCourseId is localId, need to map to firebaseId if want to link correctly on cloud
            YogaClassSession session = new YogaClassSession(
                    null, entity.getCloudDatabaseId() != null ? entity.getCloudDatabaseId() : String.valueOf(entity.getParentCourseId()),
                    entity.getClassDate(), entity.getAssignedInstructor(), entity.getClassNotes(), entity.getLocalDatabaseId()
            );
            firebaseManager.createNewClassSession(session, (error, ref) -> {
                if (error == null) {
                    entity.setCloudSyncStatus(true);
                    entity.setCloudDatabaseId(ref.getKey());
                    db.classSessionDao().updateClassSession(entity);
                }
            });
        }
    }

    /**
     * Perform data synchronization with visual feedback
     */
    private void performDataSync() {
        Toast.makeText(YogaCourseListActivity.this, "Syncing data...", Toast.LENGTH_SHORT).show();

        // Get unsynced data counts
        List<YogaCourseEntity> unsyncedCourses = db.courseDao().getUnsyncedCourses();
        List<YogaClassSessionEntity> unsyncedSessions = db.classSessionDao().getUnsyncedSessions();

        int totalUnsynced = unsyncedCourses.size() + unsyncedSessions.size();

        if (totalUnsynced == 0) {
            Toast.makeText(YogaCourseListActivity.this, "All data is already synced!", Toast.LENGTH_SHORT).show();
            buttonSync.setEnabled(true);
            return;
        }

        // Perform sync operations
        syncCoursesToFirebase();
        syncClassSessionsToFirebase();

        // Re-enable button after a delay
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            buttonSync.setEnabled(true);
            Toast.makeText(YogaCourseListActivity.this, "Sync completed!", Toast.LENGTH_SHORT).show();
            // Refresh the course list
            loadCourses();
        }, 2000); // 2 seconds delay
    }
} 