package com.example.elsa_speak_clone;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private EditText etNewUsername;
    private EditText etNewPassword;
    private EditText etRewritePassword;
    private Button btnRegisterUser;
    private LearningAppDatabase dbHelper;
    private TextView btnLogin;
    private UserSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sessionManager = new UserSessionManager(this);
        initializeViews();
        dbHelper = new LearningAppDatabase(this);
        setupRegisterButton();
        setupLoginButton();
    }

    private void initializeViews() {
        etNewUsername = findViewById(R.id.etUsername);
        etNewPassword = findViewById(R.id.etPassword);
        etRewritePassword = findViewById(R.id.etRewritePassword);
        btnRegisterUser = findViewById(R.id.btnRegisterUser);
        btnLogin = findViewById(R.id.btnLogin);
    }

    private void setupLoginButton() {
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupRegisterButton() {
        btnRegisterUser.setOnClickListener(v -> {
            String username = etNewUsername.getText().toString().trim();
            String password = etNewPassword.getText().toString().trim();
            String rewritePassword = etRewritePassword.getText().toString().trim();

            if (validateInput(username, password, rewritePassword)) {
                if (registerUser(username, password)) {
                    showToast("Registration Successful");
                    navigateToMainActivity();
                } else {
                    showToast("Registration Failed");
                }
            }
        });
    }

    private boolean validateInput(String username, String password, String rewritePassword) {
        // Validate username first
        if (!validateUsername(username)) {
            return false;
        }
        // Validate password and rewritePassword
        if (!validatePassword(password, rewritePassword)) {
            return false;
        }
       return true;
    }

    private boolean validateUsername(String username) {
        if (TextUtils.isEmpty(username)) {
            showToast("Please enter a username.");
            return false;
        }

        if (username.length() < 6 || username.length() > 32) {
            showToast("Username must be between 6 and 32 characters.");
            return false;
        }

        if (!dbHelper.isUsernameAvailable(username)) {
            showToast("Username already exists.");
            return false;
        }

        return true;
    }

    private boolean validatePassword(String password, String rewritePassword) {
        if (TextUtils.isEmpty(password)) {
            showToast("Please enter a password.");
            return false;
        }

        if (password.length() < 8 || !password.matches(".*\\d.*")) {
            showToast("Password must be at least 8 characters and contain at least one number.");
            return false;
        }

        if (!Objects.equals(password, rewritePassword)) {
            showToast("Passwords do not match.");
            return false;
        }

        return true;
    }


    private void navigateToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean registerUser(String username, String password) {
        boolean success = dbHelper.registerUser(username, password);
        if (success) {
            sessionManager.saveUserSession(username, UserSessionManager.AUTH_TYPE_LOCAL);
        }
        return success;
    }

    private void showToast(String message) {
        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
    }
   
}
