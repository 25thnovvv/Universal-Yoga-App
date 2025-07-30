package com.example.universalyogaapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.universalyogaapp.R;
import com.example.universalyogaapp.ui.auth.LoginActivity;
import com.example.universalyogaapp.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    // Configuration constants
    private static final int SPLASH_DISPLAY_DURATION = 2000; // 2 seconds
    
    // Components
    private SessionManager sessionManagerInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initializeComponents();
        scheduleNavigation();
    }

    /**
     * Initialize required components
     */
    private void initializeComponents() {
        sessionManagerInstance = new SessionManager(this);
    }

    /**
     * Schedule navigation after splash delay
     */
    private void scheduleNavigation() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            navigateToAppropriateScreen();
        }, SPLASH_DISPLAY_DURATION);
    }

    /**
     * Navigate to appropriate screen based on login status
     */
    private void navigateToAppropriateScreen() {
        Intent navigationIntent;
        
        if (sessionManagerInstance.isLoggedIn()) {
            // User is logged in, navigate to main activity
            navigationIntent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            // User is not logged in, navigate to login activity
            navigationIntent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        
        startActivity(navigationIntent);
        finish();
    }
}