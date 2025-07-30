
package com.example.universalyogaapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.universalyogaapp.R;
import com.example.universalyogaapp.ui.MainActivity;
import com.example.universalyogaapp.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    // UI Components
    private TextInputEditText usernameInputField;
    private TextInputEditText passwordInputField;
    private MaterialButton loginButton;
    private View loadingIndicator;
    
    // Business logic components
    private SessionManager sessionManagerInstance;

    // Authentication credentials
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    private static final int NETWORK_DELAY_SIMULATION = 1000; // 1 second

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeUserInterface();
        setupEventListeners();
    }

    /**
     * Initialize UI components
     */
    private void initializeUserInterface() {
        usernameInputField = findViewById(R.id.editTextUsername);
        passwordInputField = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);
        loadingIndicator = findViewById(R.id.progressBar);
        sessionManagerInstance = new SessionManager(this);
    }

    /**
     * Setup event listeners
     */
    private void setupEventListeners() {
        loginButton.setOnClickListener(v -> executeLoginProcess());
    }

    /**
     * Execute login process
     */
    private void executeLoginProcess() {
        String enteredUsername = usernameInputField.getText().toString().trim();
        String enteredPassword = passwordInputField.getText().toString().trim();

        if (!validateInputFields(enteredUsername, enteredPassword)) {
            return;
        }

        displayLoadingState(true);

        // Simulate network request
        loginButton.postDelayed(() -> {
            processAuthenticationResult(enteredUsername, enteredPassword);
        }, NETWORK_DELAY_SIMULATION);
    }

    /**
     * Validate input fields
     */
    private boolean validateInputFields(String username, String password) {
        if (TextUtils.isEmpty(username)) {
            usernameInputField.setError("Please enter username");
            usernameInputField.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInputField.setError("Please enter password");
            passwordInputField.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Process authentication result
     */
    private void processAuthenticationResult(String username, String password) {
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            handleSuccessfulLogin(username);
        } else {
            handleFailedLogin();
        }
    }

    /**
     * Handle successful login
     */
    private void handleSuccessfulLogin(String username) {
        displayLoadingState(false);
        sessionManagerInstance.setLogin(true, username);
        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

        Intent mainActivityIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
        finish();
    }

    /**
     * Handle failed login
     */
    private void handleFailedLogin() {
        displayLoadingState(false);
        Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
    }

    /**
     * Display loading state
     */
    private void displayLoadingState(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!isLoading);
    }
} 
