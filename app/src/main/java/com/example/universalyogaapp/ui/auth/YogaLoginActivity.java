package com.example.universalyogaapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.universalyogaapp.R;
import com.example.universalyogaapp.ui.YogaMainActivity;
import com.example.universalyogaapp.utils.YogaSessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class YogaLoginActivity extends AppCompatActivity {

    // UI Components
    private TextInputEditText emailInputField;
    private TextInputEditText passwordInputField;
    private MaterialButton loginButton;
    private MaterialButton registerButton;
    private MaterialButton forgotPasswordButton;
    private View loadingIndicator;
    
    // Business logic components
    private YogaSessionManager sessionManagerInstance;
    
    // Firebase Authentication
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga_login);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        initializeUserInterface();
        setupEventListeners();
    }

    /**
     * Initialize UI components
     */
    private void initializeUserInterface() {
        emailInputField = findViewById(R.id.editTextEmail);
        passwordInputField = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);
        registerButton = findViewById(R.id.buttonRegister);
        forgotPasswordButton = findViewById(R.id.buttonForgotPassword);
        loadingIndicator = findViewById(R.id.progressBar);
        sessionManagerInstance = new YogaSessionManager(this);
    }

    /**
     * Setup event listeners
     */
    private void setupEventListeners() {
        loginButton.setOnClickListener(v -> executeLoginProcess());
        registerButton.setOnClickListener(v -> navigateToRegister());
        forgotPasswordButton.setOnClickListener(v -> navigateToForgotPassword());
    }

    /**
     * Execute login process
     */
    private void executeLoginProcess() {
        String email = emailInputField.getText().toString().trim();
        String password = passwordInputField.getText().toString().trim();

        if (!validateInputFields(email, password)) {
            return;
        }

        displayLoadingState(true);

        // Sign in with Firebase Auth
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    displayLoadingState(false);
                    if (task.isSuccessful()) {
                        handleSuccessfulLogin();
                    } else {
                        handleFailedLogin(task.getException());
                    }
                });
    }

    /**
     * Validate input fields
     */
    private boolean validateInputFields(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            emailInputField.setError("Please enter email");
            emailInputField.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputField.setError("Invalid email format");
            emailInputField.requestFocus();
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
     * Handle successful login
     */
    private void handleSuccessfulLogin() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            sessionManagerInstance.setLogin(true, user.getEmail());
            Toast.makeText(YogaLoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

            Intent mainActivityIntent = new Intent(YogaLoginActivity.this, YogaMainActivity.class);
            mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainActivityIntent);
            finish();
        }
    }

    /**
     * Handle failed login
     */
    private void handleFailedLogin(Exception exception) {
        String errorMessage = "Login failed";
        if (exception != null) {
            String exceptionMessage = exception.getMessage();
            if (exceptionMessage != null) {
                if (exceptionMessage.contains("no user record")) {
                    errorMessage = "Email is not registered";
                } else if (exceptionMessage.contains("password is invalid")) {
                    errorMessage = "Incorrect password";
                } else if (exceptionMessage.contains("network")) {
                    errorMessage = "Network connection error";
                }
            }
        }
        Toast.makeText(YogaLoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    /**
     * Navigate to register activity
     */
    private void navigateToRegister() {
        Intent registerIntent = new Intent(YogaLoginActivity.this, YogaRegisterActivity.class);
        startActivity(registerIntent);
    }

    /**
     * Navigate to forgot password activity
     */
    private void navigateToForgotPassword() {
        Intent forgotPasswordIntent = new Intent(YogaLoginActivity.this, YogaForgotPasswordActivity.class);
        startActivity(forgotPasswordIntent);
    }

    /**
     * Display loading state
     */
    private void displayLoadingState(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!isLoading);
        registerButton.setEnabled(!isLoading);
        forgotPasswordButton.setEnabled(!isLoading);
    }
} 