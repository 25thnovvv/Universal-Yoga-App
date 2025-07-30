package com.example.universalyogaapp.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.universalyogaapp.R;
import com.example.universalyogaapp.ui.fragments.AdminFragment;
import com.example.universalyogaapp.ui.course.CourseListActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUserInterface();
        configureBottomNavigation();
        loadDefaultFragment();
    }

    /**
     * Initialize UI components
     */
    private void initializeUserInterface() {
        bottomNavigationView = findViewById(R.id.bottomNavigation);
    }

    /**
     * Configure bottom navigation behavior
     */
    private void configureBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_admin) {
                switchToFragment(new AdminFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_courses) {
                // Navigate directly to CourseListActivity
                Intent courseListIntent = new Intent(MainActivity.this, CourseListActivity.class);
                startActivity(courseListIntent);
                return true;
            }
            return false;
        });
    }

    /**
     * Load default fragment (Admin)
     */
    private void loadDefaultFragment() {
        switchToFragment(new AdminFragment());
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
}