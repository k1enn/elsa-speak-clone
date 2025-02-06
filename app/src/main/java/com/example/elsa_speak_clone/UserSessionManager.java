package com.example.elsa_speak_clone;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserSessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USERNAME = "loggedInUsername";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_AUTH_TYPE = "authType";
    private static final String KEY_LOCAL_USERNAME = "localUsername";
    private static final String KEY_LOCAL_IS_LOGGED_IN = "localIsLoggedIn";
    private static final String KEY_FIREBASE_EMAIL = "firebaseEmail";
    private static final String KEY_FIREBASE_IS_LOGGED_IN = "firebaseIsLoggedIn";
    
    public static final String AUTH_TYPE_FIREBASE = "firebase";
    public static final String AUTH_TYPE_LOCAL = "local";
    
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Context context;
    private final FirebaseAuth firebaseAuth;

    public UserSessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public void saveUserSession(String username, String authType) {
        if (AUTH_TYPE_LOCAL.equals(authType)) {
            editor.putString(KEY_LOCAL_USERNAME, username);
            editor.putBoolean(KEY_LOCAL_IS_LOGGED_IN, true);
        } else if (AUTH_TYPE_FIREBASE.equals(authType)) {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {
                editor.putString(KEY_FIREBASE_EMAIL, firebaseUser.getEmail());
                editor.putBoolean(KEY_FIREBASE_IS_LOGGED_IN, true);
            }
        }
        editor.putString(KEY_AUTH_TYPE, authType);
        editor.apply();
    }

    public boolean isLoggedIn() {
        String authType = getAuthType();

        if (AUTH_TYPE_FIREBASE.equals(authType)) {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            boolean firebaseLoggedIn = firebaseUser != null;
            editor.putBoolean(KEY_FIREBASE_IS_LOGGED_IN, firebaseLoggedIn);
            editor.apply();
            return firebaseLoggedIn;
        } else if (AUTH_TYPE_LOCAL.equals(authType)) {
            return pref.getBoolean(KEY_LOCAL_IS_LOGGED_IN, false);
        }
        
        return false;
    }

    public String getUsername() {
        String authType = getAuthType();

        if (AUTH_TYPE_FIREBASE.equals(authType)) {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            return firebaseUser != null ? firebaseUser.getEmail() : pref.getString(KEY_FIREBASE_EMAIL, null);
        } else if (AUTH_TYPE_LOCAL.equals(authType)) {
            return pref.getString(KEY_LOCAL_USERNAME, null);
        }
        
        return null;
    }

    public String getAuthType() {
        return pref.getString(KEY_AUTH_TYPE, "");
    }

    public void logout() {
        String authType = getAuthType();

        if (AUTH_TYPE_FIREBASE.equals(authType)) {
            firebaseAuth.signOut();
            editor.remove(KEY_FIREBASE_EMAIL);
            editor.remove(KEY_FIREBASE_IS_LOGGED_IN);
        } else if (AUTH_TYPE_LOCAL.equals(authType)) {
            editor.remove(KEY_LOCAL_USERNAME);
            editor.remove(KEY_LOCAL_IS_LOGGED_IN);
        }
        
        editor.remove(KEY_AUTH_TYPE);
        editor.apply();
    }
}