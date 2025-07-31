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

public class YogaAdminFragment extends Fragment {

    // UI Components
    private TextView usernameDisplayField;
    private MaterialButton syncDataButton;
    private MaterialButton resetDatabaseButton;
    private MaterialButton logoutButton;

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
        logoutButton = view.findViewById(R.id.buttonLogout);
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
        logoutButton.setOnClickListener(v -> executeUserLogout());
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

        new Thread(() -> {
            // Clear all data
            databaseInstance.courseDao().deleteAllCourses();
            databaseInstance.classSessionDao().deleteAllSessions();

            // Update UI on main thread
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Database reset successfully", Toast.LENGTH_SHORT).show();
                resetDatabaseButton.setEnabled(true);
                resetDatabaseButton.setText("Reset Database");
            });
        }).start();
    }

    /**
     * Execute user logout
     */
    private void executeUserLogout() {
        sessionManagerInstance.logout();
        Toast.makeText(requireContext(), "Logout successful", Toast.LENGTH_SHORT).show();

        // Navigate to LoginActivity
        requireActivity().finish();
        requireActivity().startActivity(new android.content.Intent(requireContext(),
                com.example.universalyogaapp.ui.auth.YogaLoginActivity.class));
    }
} 