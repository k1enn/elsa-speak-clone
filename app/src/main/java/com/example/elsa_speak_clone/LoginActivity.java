package com.example.elsa_speak_clone;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView btnRegister ;
    private LinearLayout googleLoginButton;
    private LearningAppDatabase dbHelper;
    private GoogleSignInHelper googleSignInHelper;
    private UserSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
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
    }

    private void initializeUI() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        googleLoginButton = findViewById(R.id.btnGoogleLogin);
        
    }

    private void initializeDatabase() {
        dbHelper = new LearningAppDatabase(this);
    }

    private void initializeGoogleLogin() {
        googleSignInHelper = new GoogleSignInHelper(this, new GoogleSignInHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                String email = user.getEmail();
                sessionManager.saveUserSession(email, UserSessionManager.AUTH_TYPE_FIREBASE);
                Toast.makeText(LoginActivity.this, "Signed in as: " + email, Toast.LENGTH_SHORT).show();
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