package com.example.elsa_speak_clone.database;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.elsa_speak_clone.activities.LoginActivity;

import java.util.HashMap;

public class SessionManager {
    private static final String TAG = "SessionManager";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    public static final String PREF_NAME = "UserSession";
    public static final String IS_LOGGED_IN = "IsLoggedIn";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_AUTH_TYPE = "authType";
    public static final String AUTH_TYPE_LOCAL = "local";
    public static final String AUTH_TYPE_GOOGLE = "google";
;

    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Save user session data for local authentication
    public void createSession(String username, int userId) {
        editor.putBoolean(IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_AUTH_TYPE, AUTH_TYPE_LOCAL);
        editor.apply();

        // Log session creation for debugging
        Log.d("SessionManager", "Created local session for user: " + username +
                ", ID: " + userId);
    }

    // Save user session data for Google authentication - ensure consistent keys
    public void createGoogleSession(String username, int userId) {
        editor.putBoolean(IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.putInt(KEY_USER_ID, userId); // Use the same key as local auth
        editor.putString(KEY_AUTH_TYPE, AUTH_TYPE_GOOGLE);
        editor.apply();

        // Log session creation for debugging
        Log.d("SessionManager", "Created Google session for user: " + username +
                ", ID: " + userId);
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(IS_LOGGED_IN, false);
    }

    // Get user data - ensure proper type conversion
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<>();
        user.put(KEY_USERNAME, sharedPreferences.getString(KEY_USERNAME, ""));
        // Ensure consistent conversion of user ID to string
        user.put(KEY_USER_ID, String.valueOf(sharedPreferences.getInt(KEY_USER_ID, -1)));
        user.put(KEY_AUTH_TYPE, sharedPreferences.getString(KEY_AUTH_TYPE, AUTH_TYPE_LOCAL));
        return user;
    }

    // Just for debugging - get the raw user ID
    public int getUserId() {
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    // Check if user is logged in via Google
    public boolean isGoogleUser() {
        String authType = sharedPreferences.getString(KEY_AUTH_TYPE, AUTH_TYPE_LOCAL);
        return AUTH_TYPE_GOOGLE.equals(authType);
    }

    // Just clear the session data without navigation (used by the activity)
    public void clearSession() {
        editor.clear();
        editor.apply();
        Log.d(TAG, "Session cleared from SharedPreferences");
    }

    // Logout user - handles navigation
    public void logout() {
        // First clear the session data
        clearSession();

        // Then handle navigation
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // Get auth type
    public String getAuthType() {
        return sharedPreferences.getString(KEY_AUTH_TYPE, AUTH_TYPE_LOCAL);
    }
}
