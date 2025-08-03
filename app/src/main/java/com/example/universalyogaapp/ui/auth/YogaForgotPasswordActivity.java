package com.example.universalyogaapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.universalyogaapp.R;
import com.example.universalyogaapp.firebase.YogaFirebaseAuthManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class YogaForgotPasswordActivity extends AppCompatActivity {

    // UI Components
    private TextInputEditText emailInputField;
    private MaterialButton resetPasswordButton;
    private MaterialButton backToLoginButton;
    private View loadingIndicator;
    
    // Firebase Auth Manager
    private YogaFirebaseAuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga_forgot_password);

        // Initialize Firebase Auth Manager
        authManager = YogaFirebaseAuthManager.getInstance(this);

        initializeUserInterface();
        setupEventListeners();
    }

    /**
     * Initialize UI components
     */
    private void initializeUserInterface() {
        emailInputField = findViewById(R.id.editTextEmail);
        resetPasswordButton = findViewById(R.id.buttonResetPassword);
        backToLoginButton = findViewById(R.id.buttonBackToLogin);
        loadingIndicator = findViewById(R.id.progressBar);
    }

    /**
     * Setup event listeners
     */
    private void setupEventListeners() {
        resetPasswordButton.setOnClickListener(v -> executeResetPasswordProcess());
        backToLoginButton.setOnClickListener(v -> navigateToLogin());
    }

    /**
     * Execute reset password process
     */
    private void executeResetPasswordProcess() {
        String email = emailInputField.getText().toString().trim();

        if (!validateInputFields(email)) {
            return;
        }

        displayLoadingState(true);

        // Send password reset email
        authManager.sendPasswordResetEmail(email, new YogaFirebaseAuthManager.OnPasswordResetListener() {
            @Override
            public void onSuccess() {
                displayLoadingState(false);
                Toast.makeText(YogaForgotPasswordActivity.this, 
                    "Password reset email has been sent. Please check your inbox.", 
                    Toast.LENGTH_LONG).show();
                navigateToLogin();
            }

            @Override
            public void onFailure(Exception exception) {
                displayLoadingState(false);
                String errorMessage = "Could not send password reset email";
                if (exception != null) {
                    String exceptionMessage = exception.getMessage();
                    if (exceptionMessage != null) {
                        if (exceptionMessage.contains("no user record")) {
                            errorMessage = "Email is not registered";
                        } else if (exceptionMessage.contains("network")) {
                            errorMessage = "Network connection error";
                        }
                    }
                }
                Toast.makeText(YogaForgotPasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Validate input fields
     */
    private boolean validateInputFields(String email) {
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

        return true;
    }

    /**
     * Navigate to login activity
     */
    private void navigateToLogin() {
        Intent loginIntent = new Intent(YogaForgotPasswordActivity.this, YogaLoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    /**
     * Display loading state
     */
    private void displayLoadingState(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        resetPasswordButton.setEnabled(!isLoading);
        backToLoginButton.setEnabled(!isLoading);
    }
} 