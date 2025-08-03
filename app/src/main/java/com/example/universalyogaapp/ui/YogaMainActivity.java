package com.example.universalyogaapp.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.universalyogaapp.R;
import com.example.universalyogaapp.firebase.YogaFirebaseAuthManager;
import com.example.universalyogaapp.ui.auth.YogaLoginActivity;
import com.example.universalyogaapp.ui.fragments.YogaAdminFragment;
import com.example.universalyogaapp.ui.course.YogaCourseListActivity;
import com.example.universalyogaapp.utils.YogaSessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class YogaMainActivity extends AppCompatActivity {

    // UI Components
    private BottomNavigationView bottomNavigationView;
    
    // Business logic components
    private YogaFirebaseAuthManager firebaseAuthManager;
    private YogaSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga_main);

        initializeUserInterface();
        configureBottomNavigation();
        loadDefaultFragment();
    }

    /**
     * Initialize UI components
     */
    private void initializeUserInterface() {
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        firebaseAuthManager = YogaFirebaseAuthManager.getInstance(this);
        sessionManager = new YogaSessionManager(this);
    }

    /**
     * Configure bottom navigation behavior
     */
    private void configureBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_admin) {
                switchToFragment(new YogaAdminFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_courses) {
                // Navigate directly to CourseListActivity
                Intent courseListIntent = new Intent(YogaMainActivity.this, YogaCourseListActivity.class);
                startActivity(courseListIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_logout) {
                handleLogout();
                return true;
            }
            return false;
        });
    }

    /**
     * Load default fragment (Admin)
     */
    private void loadDefaultFragment() {
        switchToFragment(new YogaAdminFragment());
    }

    /**
     * Switch to specified fragment
     */
    private void switchToFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    /**
     * Handle logout process
     */
    private void handleLogout() {
        // Sign out from Firebase
        firebaseAuthManager.signOut();
        
        // Clear session
        sessionManager.setLogin(false, null);
        
        // Navigate to login activity
        Intent loginIntent = new Intent(YogaMainActivity.this, YogaLoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
} 