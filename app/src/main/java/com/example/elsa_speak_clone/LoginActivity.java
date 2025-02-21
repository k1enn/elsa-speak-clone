package com.example.elsa_speak_clone;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ImageButton btnToggleLoginPassword;
    private TextView btnRegister ;
    private LinearLayout googleLoginButton;
    private LearningAppDatabase dbHelper;
    private GoogleSignInHelper googleSignInHelper;
    private UserSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize sessionManager first
        sessionManager = new UserSessionManager(this);

        // Check if already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            finish();
            return;
        }

        initializeUI();
        initializeDatabase();
        initializeGoogleLogin();
        setupLoginButton();
        setupRegisterButton();
        setupGoogleLoginButton();
        setupShowPasswordButton();
    }

    private void initializeUI() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        googleLoginButton = findViewById(R.id.btnGoogleLogin);
        btnToggleLoginPassword = findViewById(R.id.btnToggleLoginPassword);

    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupShowPasswordButton() {
        btnToggleLoginPassword.setOnClickListener(new View.OnClickListener() {
            boolean isPasswordVisible = false;

            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Hide Password
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    btnToggleLoginPassword.setImageResource(R.drawable.ic_eye_closed); // Change to closed eye icon
                    etPassword.setTypeface(null, Typeface.NORMAL); // Set to default font
                } else {
                    // Show Password
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    btnToggleLoginPassword.setImageResource(R.drawable.ic_eye_open); // Change to open eye icon
                    etPassword.setTypeface(null, Typeface.NORMAL); // Set to default font
                }
                isPasswordVisible = !isPasswordVisible;
                etPassword.setSelection(etPassword.getText().length()); // Move cursor to the end
            }
        });
    }
    private void initializeDatabase() {
        dbHelper = new LearningAppDatabase(this);
    }

    private void initializeGoogleLogin() {
        googleSignInHelper = new GoogleSignInHelper(this, new GoogleSignInHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                String email = user.getEmail();
                if (!dbHelper.doesUserGmailExist(email)) {
                    String name = user.getDisplayName() != null ? 
                        user.getDisplayName() : Objects.requireNonNull(email).split("@")[0];
                    dbHelper.registerUser(email, name);
                }
                sessionManager.saveUserSession(email, UserSessionManager.AUTH_TYPE_FIREBASE);
                navigateToMain();
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupLoginButton() {
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

               if (dbHelper.authenticateUser(username, password)) {
                   sessionManager.saveUserSession(username, UserSessionManager.AUTH_TYPE_LOCAL);
                   Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                   navigateToMain();
                   finish();
               } else {
                   Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
               } 
        });
    }

    // Google Sign-In Setup
    private void setupGoogleLoginButton() {
        googleLoginButton.setOnClickListener(v -> googleSignInHelper.signIn());
    }

    private void setupRegisterButton() {
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void navigateToSpeechToText() {
        Intent intent = new Intent(LoginActivity.this, SpeechToText.class);
        startActivity(intent);
        finish();
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // Handle Google Sign-In Result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        googleSignInHelper.handleActivityResult(requestCode, resultCode, data);
    }
}
