package com.example.universalyogaapp.ui.course;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.universalyogaapp.R;
import com.example.universalyogaapp.firebase.YogaFirebaseManager;
import com.example.universalyogaapp.model.YogaClassSession;
import com.example.universalyogaapp.db.YogaAppDatabase;
import com.example.universalyogaapp.db.YogaClassSessionEntity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class YogaClassSessionEditorActivity extends AppCompatActivity {
    // UI Components
    private EditText editTextDate, editTextTeacher, editTextNote;
    private Button buttonSave;

    // Business logic components
    private String courseId, courseSchedule; // courseSchedule: example "Tuesday"
    private YogaFirebaseManager firebaseManager;
    private YogaClassSession editingSession;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private YogaAppDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga_class_session_editor);

        initializeDatabase();
        initializeUserInterface();
        setupEventListeners();
        loadSessionData();
    }

    /**
     * Initialize database
     */
    private void initializeDatabase() {
        db = androidx.room.Room.databaseBuilder(
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
        editTextDate = findViewById(R.id.editTextDate);
        editTextTeacher = findViewById(R.id.editTextTeacher);
        editTextNote = findViewById(R.id.editTextNote);
        buttonSave = findViewById(R.id.buttonSaveSession);
        firebaseManager = new YogaFirebaseManager();
    }

    /**
     * Setup event listeners
     */
    private void setupEventListeners() {
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSession();
            }
        });
    }

    /**
     * Load session data
     */
    private void loadSessionData() {
        courseId = getIntent().getStringExtra("course_id"); // Ensure this value is firebaseId
        courseSchedule = getIntent().getStringExtra("course_schedule"); // example "Tuesday"
        editingSession = (YogaClassSession) getIntent().getSerializableExtra("class_session");

        if (editingSession != null) {
            setTitle("Edit Class Session");
            editTextDate.setText(editingSession.getDate());
            editTextTeacher.setText(editingSession.getTeacher());
            editTextNote.setText(editingSession.getNote());
        } else {
            setTitle("Add Class Session");
        }
    }

    /**
     * Show date picker dialog
     */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String dateStr = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                editTextDate.setText(dateStr);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
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
     * Save class session
     */
    private void saveSession() {
        String date = editTextDate.getText().toString().trim();
        String teacher = editTextTeacher.getText().toString().trim();
        String note = editTextNote.getText().toString().trim();

        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(teacher)) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate class date matches course schedule
        if (!isDateMatchSchedule(date, courseSchedule)) {
            Toast.makeText(this, "Selected date does not match course schedule (" + courseSchedule + ")", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editingSession == null) {
            // Add new session
            YogaClassSessionEntity entity = new YogaClassSessionEntity();
            entity.setCloudDatabaseId(null);
            entity.setParentCourseId(courseId);
            entity.setClassDate(date);
            entity.setAssignedInstructor(teacher);
            entity.setClassNotes(note);
            
            if (isNetworkAvailable()) {
                entity.setCloudSyncStatus(true);
                long localId = db.classSessionDao().insertClassSession(entity);
                YogaClassSession session = new YogaClassSession(
                        null, courseId, date, teacher, note, (int) localId
                );
                firebaseManager.createNewClassSession(session, (error, ref) -> {
                    if (error == null) {
                        db.classSessionDao().markSessionAsSynced((int) localId, ref.getKey());
                        runOnUiThread(() -> {
                            Toast.makeText(YogaClassSessionEditorActivity.this, "Class session saved and synced!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(YogaClassSessionEditorActivity.this, "Failed to sync with server, saved locally.", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                });
            } else {
                entity.setCloudSyncStatus(false);
                db.classSessionDao().insertClassSession(entity);
                Toast.makeText(this, "Class session saved locally. Please sync to upload.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            // Edit existing session
            YogaClassSessionEntity entity = db.classSessionDao().getSessionByCloudId(editingSession.getId());
            if (entity != null) {
                entity.setClassDate(date);
                entity.setAssignedInstructor(teacher);
                entity.setClassNotes(note);
                
                if (isNetworkAvailable()) {
                    entity.setCloudSyncStatus(true);
                    db.classSessionDao().updateClassSession(entity);
                    YogaClassSession session = new YogaClassSession(
                            entity.getCloudDatabaseId(), entity.getParentCourseId(), date, teacher, note, entity.getLocalDatabaseId()
                    );
                    firebaseManager.updateExistingClassSession(session, (error, ref) -> {
                        if (error == null) {
                            db.classSessionDao().markSessionAsSynced(entity.getLocalDatabaseId(), entity.getCloudDatabaseId());
                            runOnUiThread(() -> {
                                Toast.makeText(YogaClassSessionEditorActivity.this, "Class session updated and synced!", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(YogaClassSessionEditorActivity.this, "Failed to sync with server, saved locally.", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }
                    });
                } else {
                    entity.setCloudSyncStatus(false);
                    db.classSessionDao().updateClassSession(entity);
                    Toast.makeText(this, "Class session updated locally. Please sync to upload.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    /**
     * Check if date matches course schedule
     */
    private boolean isDateMatchSchedule(String dateStr, String schedule) {
        try {
            Date date = sdf.parse(dateStr);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1=Sunday, 2=Monday,...
            String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
            String dayName = days[dayOfWeek - 1];
            return schedule != null && schedule.contains(dayName);
        } catch (ParseException e) {
            return false;
        }
    }
} 