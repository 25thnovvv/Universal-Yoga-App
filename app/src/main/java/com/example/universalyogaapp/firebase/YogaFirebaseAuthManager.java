package com.example.universalyogaapp.firebase;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class YogaFirebaseAuthManager {
    
    private static YogaFirebaseAuthManager instance;
    private FirebaseAuth firebaseAuth;
    private Context context;
    
    private YogaFirebaseAuthManager(Context context) {
        this.context = context.getApplicationContext();
        this.firebaseAuth = FirebaseAuth.getInstance();
    }
    
    public static synchronized YogaFirebaseAuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new YogaFirebaseAuthManager(context);
        }
        return instance;
    }
    
    /**
     * Get current Firebase Auth instance
     */
    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }
    
    /**
     * Get current user
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }
    
    /**
     * Sign out current user
     */
    public void signOut() {
        firebaseAuth.signOut();
        Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String email, OnPasswordResetListener listener) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (listener != null) {
                            listener.onSuccess();
                        }
                    } else {
                        if (listener != null) {
                            listener.onFailure(task.getException());
                        }
                    }
                });
    }
    
    /**
     * Delete current user account
     */
    public void deleteCurrentUser(OnDeleteUserListener listener) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            user.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (listener != null) {
                                listener.onSuccess();
                            }
                        } else {
                            if (listener != null) {
                                listener.onFailure(task.getException());
                            }
                        }
                    });
        } else {
            if (listener != null) {
                listener.onFailure(new Exception("No user logged in"));
            }
        }
    }
    
    /**
     * Update user email
     */
    public void updateUserEmail(String newEmail, OnUpdateEmailListener listener) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            user.updateEmail(newEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (listener != null) {
                                listener.onSuccess();
                            }
                        } else {
                            if (listener != null) {
                                listener.onFailure(task.getException());
                            }
                        }
                    });
        } else {
            if (listener != null) {
                listener.onFailure(new Exception("No user logged in"));
            }
        }
    }
    
    /**
     * Update user password
     */
    public void updateUserPassword(String newPassword, OnUpdatePasswordListener listener) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (listener != null) {
                                listener.onSuccess();
                            }
                        } else {
                            if (listener != null) {
                                listener.onFailure(task.getException());
                            }
                        }
                    });
        } else {
            if (listener != null) {
                listener.onFailure(new Exception("No user logged in"));
            }
        }
    }
    
    // Listener interfaces
    public interface OnPasswordResetListener {
        void onSuccess();
        void onFailure(Exception exception);
    }
    
    public interface OnDeleteUserListener {
        void onSuccess();
        void onFailure(Exception exception);
    }
    
    public interface OnUpdateEmailListener {
        void onSuccess();
        void onFailure(Exception exception);
    }
    
    public interface OnUpdatePasswordListener {
        void onSuccess();
        void onFailure(Exception exception);
    }
} 