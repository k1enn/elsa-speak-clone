package com.example.elsa_speak_clone;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    Button btnLogin;
    Button btnSpeechToText;
    TextView tvDayStreak;
    TextView tvXPPoint;
    TextView tvWelcome;
    private LearningAppDatabase databaseHelper;
    private UserSessionManager sessionManager;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        initializeUI();
        initializeVariables();

        if(!CheckLoginState())
            navigateToLogin();
        else
            WelcomeUsername();

        loadUserProgress();
        setupLoginButton();
        setupSpeechToTextButton();
    }

    private void WelcomeUsername() {
        tvWelcome.setText("Welcome back " + username + "!");
    }

    private void initializeVariables() {
        sessionManager = new UserSessionManager(this);
        databaseHelper = new LearningAppDatabase(this);
        // Use for testing
        databaseHelper.injectProgress("github_k1enn", 69, 69);
        username = sessionManager.getUsername();

    }
    private boolean CheckLoginState() {
        if (!sessionManager.isLoggedIn()) {           
            return false;
        }
        return true;
    }
    private void initializeUI() {
        btnLogin = findViewById(R.id.btnLogin);
        btnSpeechToText = findViewById(R.id.btnSpeechToText);
        tvDayStreak = findViewById(R.id.tvDayStreak);
        tvXPPoint = findViewById(R.id.tvXPPoint);
        tvWelcome = findViewById(R.id.tvWelcome);
    }

    private void loadUserProgress() {
        if (username != null) {
            Cursor cursor = databaseHelper.getUserProgress(username);
            if (cursor != null && cursor.moveToFirst()) {
                try {
                    int xp = cursor.getInt(cursor.getColumnIndexOrThrow("xp_points"));
                    int streak = cursor.getInt(cursor.getColumnIndexOrThrow("day_streak"));

                    // Convert to String when setting text
                    tvXPPoint.setText(String.valueOf(xp));
                    tvDayStreak.setText(String.valueOf(streak));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    cursor.close();
                }
            }
        }
    }

    private void setupSpeechToTextButton() {
        btnSpeechToText.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                Intent intent = new Intent(MainActivity.this, SpeechToText.class);
                startActivity(intent);
            } else {
                navigateToLogin();
            }
        });
    }
    
    public void setupLoginButton() {
        if(CheckLoginState()) {
            btnLogin.setText("Logout");
        }
        btnLogin.setOnClickListener(v -> {
            sessionManager.logout();
            navigateToLogin();
            finish();
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
