package com.example.universalyogaapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class YogaSessionManager {
    // SharedPreferences configuration
    private static final String SHARED_PREFERENCES_NAME = "YogaAppSession";
    private static final String KEY_LOGIN_STATUS = "isLoggedIn";
    private static final String KEY_CURRENT_USERNAME = "username";

    // SharedPreferences instances
    private final SharedPreferences sharedPreferencesInstance;
    private final SharedPreferences.Editor sharedPreferencesEditor;
    private final Context applicationContext;

    /**
     * Constructor for SessionManager
     */
    public YogaSessionManager(Context context) {
        this.applicationContext = context;
        sharedPreferencesInstance = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferencesInstance.edit();
    }

    /**
     * Sets user login status and username
     */
    public void setUserLoginStatus(boolean isUserLoggedIn, String username) {
        sharedPreferencesEditor.putBoolean(KEY_LOGIN_STATUS, isUserLoggedIn);
        sharedPreferencesEditor.putString(KEY_CURRENT_USERNAME, username);
        sharedPreferencesEditor.commit();
    }

    /**
     * Checks if user is currently logged in
     */
    public boolean checkUserLoginStatus() {
        return sharedPreferencesInstance.getBoolean(KEY_LOGIN_STATUS, false);
    }

    /**
     * Gets current logged in username
     */
    public String getCurrentUsername() {
        return sharedPreferencesInstance.getString(KEY_CURRENT_USERNAME, "");
    }

    /**
     * Logs out user by clearing all session data
     */
    public void performUserLogout() {
        sharedPreferencesEditor.clear();
        sharedPreferencesEditor.commit();
    }

    // Legacy methods for backward compatibility
    public void setLogin(boolean isLoggedIn, String username) {
        setUserLoginStatus(isLoggedIn, username);
    }

    public boolean isLoggedIn() {
        return checkUserLoginStatus();
    }

    public String getUsername() {
        return getCurrentUsername();
    }

    public void logout() {
        performUserLogout();
    }
} 