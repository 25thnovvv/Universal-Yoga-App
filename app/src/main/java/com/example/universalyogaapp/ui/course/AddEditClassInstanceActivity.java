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
import com.example.universalyogaapp.firebase.FirebaseManager;
import com.example.universalyogaapp.model.ClassInstance;
import com.example.universalyogaapp.db.AppDatabase;
import com.example.universalyogaapp.db.ClassInstanceEntity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class AddEditClassInstanceActivity extends AppCompatActivity {
    // UI Components
    private EditText editTextDate, editTextTeacher, editTextNote;
    private Button buttonSave;

    // Business logic components
    private String courseId, courseSchedule; // courseSchedule: example "Tuesday"
    private FirebaseManager firebaseManager;
    private ClassInstance editingInstance;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private AppDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_class_instance);

        initializeDatabase();
        initializeUserInterface();
        setupEventListeners();
        loadInstanceData();
    }

    /**
     * Initialize database
     */
    private void initializeDatabase() {
        db = androidx.room.Room.databaseBuilder(
                        getApplicationContext(),
                        AppDatabase.class,
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
        buttonSave = findViewById(R.id.buttonSaveInstance);
        firebaseManager = new FirebaseManager();
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
                saveInstance();
            }
        });
    }

    /**
     * Load instance data
     */
    private void loadInstanceData() {
        courseId = getIntent().getStringExtra("course_id"); // Ensure this value is firebaseId
        courseSchedule = getIntent().getStringExtra("course_schedule"); // example "Tuesday"
        editingInstance = (ClassInstance) getIntent().getSerializableExtra("class_instance");

        if (editingInstance != null) {
            setTitle("Edit Class Session");
            editTextDate.setText(editingInstance.getDate());
            editTextTeacher.setText(editingInstance.getTeacher());
            editTextNote.setText(editingInstance.getNote());
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
     * Save class instance
     */
    private void saveInstance() {
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

        if (editingInstance == null) {
            // Add new instance
            ClassInstanceEntity entity = new ClassInstanceEntity();
            entity.setCloudDatabaseId(null);
            entity.setParentCourseId(courseId);
            entity.setClassDate(date);
            entity.setAssignedInstructor(teacher);
            entity.setClassNotes(note);
            
            if (isNetworkAvailable()) {
                entity.setCloudSyncStatus(true);
                long localId = db.classInstanceDao().insertClassInstance(entity);
                ClassInstance instance = new ClassInstance(
                        null, courseId, date, teacher, note, (int) localId
                );
                firebaseManager.createNewClassInstance(instance, (error, ref) -> {
                    if (error == null) {
                        db.classInstanceDao().markInstanceAsSynced((int) localId, ref.getKey());
                        runOnUiThread(() -> {
                            Toast.makeText(AddEditClassInstanceActivity.this, "Class session saved and synced!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(AddEditClassInstanceActivity.this, "Failed to sync with server, saved locally.", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                });
            } else {
                entity.setCloudSyncStatus(false);
                db.classInstanceDao().insertClassInstance(entity);
                Toast.makeText(this, "Class session saved locally. Please sync to upload.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            // Edit existing instance
            ClassInstanceEntity entity = db.classInstanceDao().getInstanceByCloudId(editingInstance.getId());
            if (entity != null) {
                entity.setClassDate(date);
                entity.setAssignedInstructor(teacher);
                entity.setClassNotes(note);
                
                if (isNetworkAvailable()) {
                    entity.setCloudSyncStatus(true);
                    db.classInstanceDao().updateClassInstance(entity);
                    ClassInstance instance = new ClassInstance(
                            entity.getCloudDatabaseId(), entity.getParentCourseId(), date, teacher, note, entity.getLocalDatabaseId()
                    );
                    firebaseManager.updateExistingClassInstance(instance, (error, ref) -> {
                        if (error == null) {
                            db.classInstanceDao().markInstanceAsSynced(entity.getLocalDatabaseId(), entity.getCloudDatabaseId());
                            runOnUiThread(() -> {
                                Toast.makeText(AddEditClassInstanceActivity.this, "Class session updated and synced!", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(AddEditClassInstanceActivity.this, "Failed to sync with server, saved locally.", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }
                    });
                } else {
                    entity.setCloudSyncStatus(false);
                    db.classInstanceDao().updateClassInstance(entity);
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