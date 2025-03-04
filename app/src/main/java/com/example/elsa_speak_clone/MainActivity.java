package com.example.elsa_speak_clone;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

// Icon lib
import com.malinskiy.materialicons.IconDrawable;
import com.malinskiy.materialicons.Iconify;
//import com.malinskiy.materialicons.IconValue;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    boolean isLoggedIn;
    int userStreak;
    String TAG = "MainActivity";

    Button btnLogin;
    CardView cvVocabulary;
    CardView cvGrammar;
    CardView cvPronunciation;
    TextView tvDayStreak;
    TextView tvXPPoint;
    TextView tvWelcome;
    ImageView ivPronunciation;
    ImageView profileImage;
    private LearningAppDatabase databaseHelper;
    private UserSessionManager sessionManager;
    int user_id;
    IconDrawable iconPronunciation;
    IconDrawable iconProfile;
    boolean loginCheck;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            initializeUI();
            initializeVariables();


            try {
                if (databaseHelper.getCurrentUsername(this) != null) {
                    WelcomeUsername();
                    updateUserStreak();
                } else {
                    Log.d(TAG, "Username is NULL");
                }
            } catch (Exception e) {
                Log.d(TAG, "Error in WelcomeUsername or updateUserStreak: ", e);
            }




            setupLoginButton();
            setupSpeechToTextButton();
            setupGrammarButton();
            setupVocabularyButton();
            loadUserProgress();
            
        } catch (Exception e) {
            Log.e("MainActivity", "Error in onCreate: ", e);
            // Handle fatal errors
            Toast.makeText(this, "An error occurred. Please try again.", Toast.LENGTH_LONG).show();
            navigateToLogin();
            finish();
        }
    }

    private void checkLogin() {
        try {
            if (!isLoggedIn) {
                navigateToLogin();
                Log.d(TAG, "Haven't logged in yet");
            }
        } catch (Exception e) {
            Log.d(TAG, "Error in check login:", e);
        }
    }
    private void WelcomeUsername() {
        try {
            if (userStreak <= 0) {
                tvWelcome.setText("Welcome " + getCurrentUsernameInMain() + "!");
            } else {
                tvWelcome.setText("Welcome back " + getCurrentUsernameInMain() + "!");
            }
        } catch (Exception e) {
            Log.d("MainActivity", "Error on WelcomeUsername" + e.getMessage());
        }

    }

    private String getCurrentUsernameInMain() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("current_user_id", -1);

        if (currentUserId == -1) {
            Log.d(TAG, "No user is logged in");
            return null;
        }

        return databaseHelper.getUsernameById(currentUserId);
    }
    private void updateUserStreak() {
        // Update streak everytime Login
        databaseHelper.updateUserStreak(this);
    }
    private void initializeVariables() {
        try {
            isLoggedIn = getIntent().getBooleanExtra("IS_LOGGED_IN", false);
            databaseHelper = new LearningAppDatabase(this);
        } catch (Exception e) {
            Log.e("MainActivity", "Error in initializeVariables: ", e);

        }
    }
    private boolean CheckLoginState() {
        return sessionManager.isLoggedIn();
    }
    private void initializeUI() {
        try {
            btnLogin = findViewById(R.id.btnLogin);
            cvPronunciation = findViewById(R.id.cvPronunciation);
            cvGrammar = findViewById(R.id.cvGrammar);
            cvVocabulary = findViewById(R.id.cvVocabulary);
            tvDayStreak = findViewById(R.id.tvDayStreak);
            tvXPPoint = findViewById(R.id.tvXPPoint);
            tvWelcome = findViewById(R.id.tvWelcome);
        } catch (Exception e)  {
            Log.d(TAG, "initializedUI components");
        }

        try {
            ivPronunciation = findViewById(R.id.ivPronunciation);
            iconPronunciation = new IconDrawable(this, Iconify.IconValue.zmdi_volume_up)
                    .colorRes(R.color.real_purple)  // Set color
                    .sizeDp(48); // Set size
            iconPronunciation.setStyle(Paint.Style.FILL);
            ivPronunciation.setImageDrawable(iconPronunciation);

            profileImage = findViewById(R.id.profileImage);
            iconProfile = new IconDrawable(this, Iconify.IconValue.zmdi_account_circle)
                    .colorRes(R.color.gray)  // Set color
                    .sizeDp(70); // Set size
            iconProfile.setStyle(Paint.Style.FILL);
            profileImage.setImageDrawable(iconProfile);
        } catch (NullPointerException npe) {
            Log.d(TAG, "Can't find icons" + npe);
        }


    }

    private void loadUserProgress() {


                try {
                    // Set text and convert to String when setting text
                    tvXPPoint.setText(String.valueOf(databaseHelper.getUserXp(this)));
                    tvDayStreak.setText(String.valueOf(databaseHelper.getUserStreak(this)));
                    Log.d(TAG, "Loaded progress");
                }

         catch (Exception e) {
            Log.e(TAG,"Error in loadUserProgress: ", e);
            // Set default values in case of any error
            tvXPPoint.setText("0");
            tvDayStreak.setText("0");
        }
    }


    private void setupSpeechToTextButton() {
        cvPronunciation.setOnClickListener(v -> {
            checkLogin();
             try {
                if (databaseHelper.getCurrentUsername(this) != null){
                    try {
                        navigateToSpeechToText();
                    } catch (Exception e) {
                        Log.d(TAG, "Can not found activity: " + e);
                    }
                } else {
                    try {
                        navigateToLogin();
                    } catch (Exception e) {
                        Log.d(TAG, "Can not found activity: " + e);
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "SpeechToText button error: " + e);
            }
        });
    }

    private void setupGrammarButton() {
        cvGrammar.setOnClickListener(v -> {
            /*try {
                if (sessionManager.isLoggedIn()) {
                    try {
                        navigateToSpeechToText();
                    } catch (Exception e) {
                        Log.d(TAG, "Can not found activity: " + e);
                    }
                } else {
                    try {
                        navigateToLogin();
                    } catch (Exception e) {
                        Log.d(TAG, "Can not found activity: " + e);
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "Grammar button error: " + e);
            }*/

                databaseHelper.injectUserStreak(user_id, 9999);
                databaseHelper.updateUserStreak(this);
        });
    }

    private void navigateToSpeechToText() {
        Intent intent = new Intent(MainActivity.this, SpeechToText.class);
        startActivity(intent);
    }
    private void setupVocabularyButton() {
        cvVocabulary.setOnClickListener(v -> {
            checkLogin();
            try {
                if (databaseHelper.getCurrentUsername(this) != null){
                    try {
                        navigateToSpeechToText();
                    } catch (Exception e) {
                        Log.d(TAG, "Can not found activity: " + e);
                    }
                } else {
                    try {
                        navigateToLogin();
                    } catch (Exception e) {
                        Log.d(TAG, "Can not found activity: " + e);
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "Vocabulary button error: " + e);
            }
        });
    }


    public void setupLoginButton() {
        btnLogin.setOnClickListener(v -> {
            try {
                if (!databaseHelper.loginCheck(this) || databaseHelper.logOut(this)) {
                    navigateToLogin();
                    finish();
                } else {
                    navigateToLogin();
                }
            } catch (Exception e) {
                Log.d(TAG, "Error in setupLoginButton", e);
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
