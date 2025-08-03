package com.example.universalyogaapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.universalyogaapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class YogaRegisterActivity extends AppCompatActivity {

    // UI Components
    private TextInputEditText emailInputField;
    private TextInputEditText passwordInputField;
    private TextInputEditText confirmPasswordInputField;
    private MaterialButton registerButton;
    private MaterialButton backToLoginButton;
    private View loadingIndicator;
    
    // Firebase Authentication
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga_register);

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
        confirmPasswordInputField = findViewById(R.id.editTextConfirmPassword);
        registerButton = findViewById(R.id.buttonRegister);
        backToLoginButton = findViewById(R.id.buttonBackToLogin);
        loadingIndicator = findViewById(R.id.progressBar);
    }

    /**
     * Setup event listeners
     */
    private void setupEventListeners() {
        registerButton.setOnClickListener(v -> executeRegisterProcess());
        backToLoginButton.setOnClickListener(v -> navigateToLogin());
    }

    /**
     * Execute register process
     */
    private void executeRegisterProcess() {
        String email = emailInputField.getText().toString().trim();
        String password = passwordInputField.getText().toString().trim();
        String confirmPassword = confirmPasswordInputField.getText().toString().trim();

        if (!validateInputFields(email, password, confirmPassword)) {
            return;
        }

        displayLoadingState(true);

        // Create user with Firebase Auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    displayLoadingState(false);
                    if (task.isSuccessful()) {
                        handleSuccessfulRegistration();
                    } else {
                        handleFailedRegistration(task.getException());
                    }
                });
    }

    /**
     * Validate input fields
     */
    private boolean validateInputFields(String email, String password, String confirmPassword) {
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

        if (password.length() < 6) {
            passwordInputField.setError("Password must be at least 6 characters");
            passwordInputField.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordInputField.setError("Please confirm password");
            confirmPasswordInputField.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInputField.setError("Passwords do not match");
            confirmPasswordInputField.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Handle successful registration
     */
    private void handleSuccessfulRegistration() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // Send email verification
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(YogaRegisterActivity.this, 
                                "Registration successful! Please check your email to verify your account.", 
                                Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(YogaRegisterActivity.this, 
                                "Registration successful but could not send verification email.", 
                                Toast.LENGTH_LONG).show();
                        }
                        navigateToLogin();
                    });
        }
    }

    /**
     * Handle failed registration
     */
    private void handleFailedRegistration(Exception exception) {
        String errorMessage = "Registration failed";
        if (exception != null) {
            String exceptionMessage = exception.getMessage();
            if (exceptionMessage != null) {
                if (exceptionMessage.contains("email address is already in use")) {
                    errorMessage = "Email is already in use";
                } else if (exceptionMessage.contains("password is invalid")) {
                    errorMessage = "Invalid password";
                } else if (exceptionMessage.contains("network")) {
                    errorMessage = "Network connection error";
                }
            }
        }
        Toast.makeText(YogaRegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    /**
     * Navigate to login activity
     */
    private void navigateToLogin() {
        Intent loginIntent = new Intent(YogaRegisterActivity.this, YogaLoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    /**
     * Display loading state
     */
    private void displayLoadingState(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!isLoading);
        backToLoginButton.setEnabled(!isLoading);
    }
}
