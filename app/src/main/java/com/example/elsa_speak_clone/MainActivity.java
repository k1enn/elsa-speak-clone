package com.example.elsa_speak_clone;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

// Icon lib
import com.malinskiy.materialicons.IconDrawable;
import com.malinskiy.materialicons.Iconify;
//import com.malinskiy.materialicons.IconValue;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

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
    String username;
    IconDrawable iconPronunciation;
    IconDrawable iconProfile;

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
        setupGrammarButton();
        setupVocabularyButton();
    }

    private void WelcomeUsername() {
        tvWelcome.setText("Welcome back " + username + "!");
    }

    private void initializeVariables() {
        sessionManager = new UserSessionManager(this);
        databaseHelper = new LearningAppDatabase(this);
        // Use for testing
        // databaseHelper.injectProgress("github_k1enn", 69, 69);
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
        cvPronunciation = findViewById(R.id.cvPronunciation);
        cvGrammar = findViewById(R.id.cvGrammar);
        cvVocabulary = findViewById(R.id.cvVocabulary);
        tvDayStreak = findViewById(R.id.tvDayStreak);
        tvXPPoint = findViewById(R.id.tvXPPoint);
        tvWelcome = findViewById(R.id.tvWelcome);

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
        cvPronunciation.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                Intent intent = new Intent(MainActivity.this, SpeechToText.class);
                startActivity(intent);
            } else {
                navigateToLogin();
            }
        });
    }

    private void setupGrammarButton() {
        cvGrammar.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                Intent intent = new Intent(MainActivity.this, SpeechToText.class);
                startActivity(intent);
            } else {
                navigateToLogin();
            }
        });
    }

    private void setupVocabularyButton() {
        cvVocabulary.setOnClickListener(v -> {
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
