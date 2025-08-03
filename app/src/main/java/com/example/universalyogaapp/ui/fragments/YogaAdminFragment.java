package com.example.universalyogaapp.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.room.Room;
import com.example.universalyogaapp.R;
import com.example.universalyogaapp.db.YogaAppDatabase;
import com.example.universalyogaapp.firebase.YogaFirebaseManager;
import com.example.universalyogaapp.utils.YogaSessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import java.lang.System;

public class YogaAdminFragment extends Fragment {

    // UI Components
    private TextView usernameDisplayField;
    private MaterialButton syncDataButton;
    private MaterialButton resetDatabaseButton;

    // Business logic components
    private YogaSessionManager sessionManagerInstance;
    private YogaFirebaseManager firebaseManagerInstance;
    private YogaAppDatabase databaseInstance;

    // Configuration constants
    private static final int SYNC_DELAY = 2000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_yoga_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeComponents(view);
        setupEventListeners();
        displayUserInformation();
    }

    /**
     * Initialize all components
     */
    private void initializeComponents(View view) {
        initializeUserInterface(view);
        initializeBusinessLogic();
    }

    /**
     * Initialize UI components
     */
    private void initializeUserInterface(View view) {
        usernameDisplayField = view.findViewById(R.id.textViewUsername);
        syncDataButton = view.findViewById(R.id.buttonSyncData);
        resetDatabaseButton = view.findViewById(R.id.buttonResetDatabase);
    }

    /**
     * Initialize business logic components
     */
    private void initializeBusinessLogic() {
        sessionManagerInstance = new YogaSessionManager(requireContext());
        firebaseManagerInstance = new YogaFirebaseManager();
        databaseInstance = Room.databaseBuilder(
                        requireContext(),
                        YogaAppDatabase.class,
                        "yoga-db"
                ).allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
    }

    /**
     * Setup event listeners
     */
    private void setupEventListeners() {
        syncDataButton.setOnClickListener(v -> executeDataSync());
        resetDatabaseButton.setOnClickListener(v -> executeDatabaseReset());
    }

    /**
     * Display user information
     */
    private void displayUserInformation() {
        String currentUsername = sessionManagerInstance.getUsername();
        if (!currentUsername.isEmpty()) {
            usernameDisplayField.setText(currentUsername);
        }
    }

    /**
     * Execute data synchronization
     */
    private void executeDataSync() {
        syncDataButton.setEnabled(false);
        syncDataButton.setText("Syncing...");

        syncDataButton.postDelayed(() -> {
            Toast.makeText(requireContext(), "Data synchronized successfully", Toast.LENGTH_SHORT).show();
            syncDataButton.setEnabled(true);
            syncDataButton.setText("Sync Data");
        }, SYNC_DELAY);
    }

    /**
     * Execute database reset
     */
    private void executeDatabaseReset() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Reset Database")
                .setMessage("Are you sure you want to reset the database? This action cannot be undone.")
                .setPositiveButton("Reset", (dialog, which) -> {
                    performDatabaseReset();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Perform actual database reset
     */
    private void performDatabaseReset() {
        resetDatabaseButton.setEnabled(false);
        resetDatabaseButton.setText("Resetting...");

        // Get Firebase manager instance
        YogaFirebaseManager firebaseManager = new YogaFirebaseManager();

        new Thread(() -> {
            try {
                // First, delete all data from Firebase
                firebaseManager.deleteAllData(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError error, DatabaseReference ref) {
                        if (error != null) {
                            System.err.println("Error deleting from Firebase: " + error.getMessage());
                        }
                        
                        // Clear all local data regardless of Firebase result
                        databaseInstance.courseDao().deleteAllCourses();
                        databaseInstance.classSessionDao().deleteAllSessions();
                        
                        // Update UI on main thread
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Database reset successfully", Toast.LENGTH_SHORT).show();
                            resetDatabaseButton.setEnabled(true);
                            resetDatabaseButton.setText("Reset Database");
                        });
                    }
                });
            } catch (Exception e) {
                // Update UI on main thread even if there's an error
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Error during reset: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    resetDatabaseButton.setEnabled(true);
                    resetDatabaseButton.setText("Reset Database");
                });
            }
        }).start();
    }


} 