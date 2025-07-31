package com.example.universalyogaapp.ui.course;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.example.universalyogaapp.R;
import com.example.universalyogaapp.firebase.YogaFirebaseManager;
import com.example.universalyogaapp.model.YogaCourse;
import com.example.universalyogaapp.utils.YogaDateUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import android.app.TimePickerDialog;
import java.util.Calendar;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import androidx.room.Room;
import com.example.universalyogaapp.db.YogaAppDatabase;
import com.example.universalyogaapp.db.YogaCourseEntity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class YogaCourseEditorActivity extends AppCompatActivity {
    // UI Components
    private TextInputEditText editTextName, editTextTime, editTextTeacher, editTextCapacity, editTextPrice, editTextDuration, editTextDescription, editTextNote;
    private ChipGroup chipGroupSchedule;
    private Button buttonSave;

    // Business logic components
    private YogaFirebaseManager firebaseManager;
    private YogaCourse editingCourse;
    private String courseId;
    private YogaAppDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga_course_editor);

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
                .fallbackToDestructiveMigration()
                .build();
    }

    /**
     * Initialize UI components
     */
    private void initializeUserInterface() {
        editTextName = findViewById(R.id.editTextName);
        chipGroupSchedule = findViewById(R.id.chipGroupSchedule);
        editTextTime = findViewById(R.id.editTextTime);
        editTextTeacher = findViewById(R.id.editTextTeacher);
        editTextCapacity = findViewById(R.id.editTextCapacity);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextDuration = findViewById(R.id.editTextDuration);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextNote = findViewById(R.id.editTextNote);
        buttonSave = findViewById(R.id.buttonSave);

        firebaseManager = new YogaFirebaseManager();
    }

    /**
     * Setup event listeners
     */
    private void setupEventListeners() {
        editTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });
        editTextTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showTimePicker();
                }
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmDialog();
            }
        });
    }

    /**
     * Load course data
     */
    private void loadCourseData() {
        courseId = getIntent().getStringExtra("course_id");
        if (courseId != null) {
            setTitle("Edit Course");
            loadCourse(courseId);
        } else {
            setTitle("Add New Course");
        }
    }

    /**
     * Load course from Firebase
     */
    private void loadCourse(String id) {
        firebaseManager.fetchCourseById(id, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                editingCourse = snapshot.getValue(YogaCourse.class);
                if (editingCourse != null) {
                    editingCourse.setId(snapshot.getKey());
                    fillCourseData(editingCourse);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(YogaCourseEditorActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Fill course data into UI
     */
    private void fillCourseData(YogaCourse course) {
        editTextName.setText(course.getName());

        // Find and check the corresponding chips
        String schedule = course.getSchedule();
        if (schedule != null && !schedule.isEmpty()) {
            List<String> selectedDays = Arrays.asList(schedule.split(","));
            for (int i = 0; i < chipGroupSchedule.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupSchedule.getChildAt(i);
                if (selectedDays.contains(chip.getText().toString())) {
                    chip.setChecked(true);
                }
            }
        }

        editTextTime.setText(course.getTime());
        editTextTeacher.setText(course.getTeacher());
        editTextCapacity.setText(String.valueOf(course.getCapacity()));
        editTextPrice.setText(String.valueOf(course.getPrice()));
        editTextDuration.setText(String.valueOf(course.getDuration()));
        editTextDescription.setText(course.getDescription());
        editTextNote.setText(course.getNote());
    }

    /**
     * Check network availability
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Show confirmation dialog
     */
    private void showConfirmDialog() {
        String name = editTextName.getText().toString().trim();
        List<String> selectedChips = new java.util.ArrayList<>();
        for (int id : chipGroupSchedule.getCheckedChipIds()) {
            Chip chip = chipGroupSchedule.findViewById(id);
            selectedChips.add(chip.getText().toString());
        }
        String schedule = String.join(",", selectedChips);
        String upcomingDate = YogaDateUtils.calculateNextUpcomingDate(schedule);
        String time = editTextTime.getText().toString().trim();
        String teacher = editTextTeacher.getText().toString().trim();
        String capacityStr = editTextCapacity.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        String durationStr = editTextDuration.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String note = editTextNote.getText().toString().trim();
        
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(schedule) || TextUtils.isEmpty(teacher) ||
                TextUtils.isEmpty(capacityStr) || TextUtils.isEmpty(priceStr) ||
                TextUtils.isEmpty(durationStr)) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        StringBuilder message = new StringBuilder();
        message.append("Name: ").append(name).append("\n");
        message.append("Schedule: ").append(schedule).append("\n");
        message.append("Time: ").append(time).append("\n");
        message.append("Teacher: ").append(teacher).append("\n");
        message.append("Capacity: ").append(capacityStr).append("\n");
        message.append("Price: ").append(priceStr).append("\n");
        message.append("Duration: ").append(durationStr).append("\n");
        message.append("Description: ").append(description).append("\n");
        message.append("Note: ").append(note).append("\n");
        
        new AlertDialog.Builder(this)
                .setTitle("Confirm Course Details")
                .setMessage(message.toString())
                .setPositiveButton("Confirm", (dialog, which) -> saveCourse())
                .setNegativeButton("Edit", null)
                .show();
    }

    /**
     * Save course to database and Firebase
     */
    private void saveCourse() {
        String name = editTextName.getText().toString().trim();
        List<String> selectedChips = new java.util.ArrayList<>();
        for (int id : chipGroupSchedule.getCheckedChipIds()) {
            Chip chip = chipGroupSchedule.findViewById(id);
            selectedChips.add(chip.getText().toString());
        }
        String schedule = String.join(",", selectedChips);
        String upcomingDate = YogaDateUtils.calculateNextUpcomingDate(schedule);
        String time = editTextTime.getText().toString().trim();
        String teacher = editTextTeacher.getText().toString().trim();
        String capacityStr = editTextCapacity.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        String durationStr = editTextDuration.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String note = editTextNote.getText().toString().trim();
        int capacity = Integer.parseInt(capacityStr);
        double price = Double.parseDouble(priceStr);
        int duration = Integer.parseInt(durationStr);

        if (editingCourse != null) {
            // EDITING EXISTING COURSE
            YogaCourse course = new YogaCourse(
                    editingCourse.getId(), name, schedule, time, teacher,
                    capacity, price, duration, description, note, upcomingDate, editingCourse.getLocalId()
            );

            if (isNetworkAvailable()) {
                // ONLINE: Update Firebase first, then local
                DatabaseReference.CompletionListener listener = (error, ref) -> {
                    if (error == null) {
                        // Update local database
                        YogaCourseEntity entity = new YogaCourseEntity();
                        entity.setLocalDatabaseId(editingCourse.getLocalId());
                        entity.setCloudDatabaseId(editingCourse.getId());
                        entity.setCourseName(name);
                        entity.setWeeklySchedule(schedule);
                        entity.setClassTime(time);
                        entity.setInstructorName(teacher);
                        entity.setMaxStudents(capacity);
                        entity.setCoursePrice(price);
                        entity.setSessionDuration(duration);
                        entity.setCourseDescription(description);
                        entity.setAdditionalNotes(note);
                        entity.setNextClassDate(upcomingDate);
                        entity.setCloudSyncStatus(true);

                        db.courseDao().updateCourse(entity);
                        runOnUiThread(() -> {
                            Toast.makeText(YogaCourseEditorActivity.this, "Course updated and synced!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(YogaCourseEditorActivity.this, "Failed to sync with server, saved locally.", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                };
                firebaseManager.updateExistingCourse(course, listener);
            } else {
                // OFFLINE: Update local only
                YogaCourseEntity entity = new YogaCourseEntity();
                entity.setLocalDatabaseId(editingCourse.getLocalId());
                entity.setCloudDatabaseId(editingCourse.getId());
                entity.setCourseName(name);
                entity.setWeeklySchedule(schedule);
                entity.setClassTime(time);
                entity.setInstructorName(teacher);
                entity.setMaxStudents(capacity);
                entity.setCoursePrice(price);
                entity.setSessionDuration(duration);
                entity.setCourseDescription(description);
                entity.setAdditionalNotes(note);
                entity.setNextClassDate(upcomingDate);
                entity.setCloudSyncStatus(false);

                db.courseDao().updateCourse(entity);
                Toast.makeText(YogaCourseEditorActivity.this, "Course updated locally. Please sync to upload.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            // CREATING NEW COURSE
            YogaCourseEntity entity = new YogaCourseEntity();
            entity.setCourseName(name);
            entity.setWeeklySchedule(schedule);
            entity.setClassTime(time);
            entity.setInstructorName(teacher);
            entity.setMaxStudents(capacity);
            entity.setCoursePrice(price);
            entity.setSessionDuration(duration);
            entity.setCourseDescription(description);
            entity.setAdditionalNotes(note);
            entity.setNextClassDate(upcomingDate);

            if (isNetworkAvailable()) {
                // ONLINE: Save local with isSynced=true, push to Firebase
                entity.setCloudSyncStatus(true);
                long localId = db.courseDao().insertCourse(entity);
                YogaCourse course = new YogaCourse(
                        entity.getCloudDatabaseId(), entity.getCourseName(), entity.getWeeklySchedule(), entity.getClassTime(), entity.getInstructorName(),
                        entity.getMaxStudents(), entity.getCoursePrice(), entity.getSessionDuration(), entity.getCourseDescription(), entity.getAdditionalNotes(), entity.getNextClassDate(), entity.getLocalDatabaseId()
                );
                DatabaseReference.CompletionListener listener = (error, ref) -> {
                    if (error == null) {
                        db.courseDao().markCourseAsSynced((int) localId, ref.getKey());
                        runOnUiThread(() -> {
                            Toast.makeText(YogaCourseEditorActivity.this, "Course saved and synced!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(YogaCourseEditorActivity.this, "Failed to sync with server, saved locally.", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                };
                if (entity.getCloudDatabaseId() == null || entity.getCloudDatabaseId().isEmpty()) {
                    firebaseManager.createNewCourse(course, listener);
                } else {
                    course.setId(entity.getCloudDatabaseId());
                    firebaseManager.updateExistingCourse(course, listener);
                }
            } else {
                // OFFLINE: Save local with isSynced=false
                entity.setCloudSyncStatus(false);
                db.courseDao().insertCourse(entity);
                Toast.makeText(YogaCourseEditorActivity.this, "Course saved locally. Please sync to upload.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * Show time picker dialog
     */
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            String time = String.format("%02d:%02d", hourOfDay, minute1);
            editTextTime.setText(time);
        }, hour, minute, true);
        timePickerDialog.show();
    }
} 