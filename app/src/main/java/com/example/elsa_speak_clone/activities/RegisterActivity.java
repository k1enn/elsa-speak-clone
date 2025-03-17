package com.example.elsa_speak_clone.activities;

import static com.example.elsa_speak_clone.database.GoogleSignInHelper.RC_SIGN_IN;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.database.GoogleSignInHelper;
import com.example.elsa_speak_clone.database.LearningAppDatabase;
import com.example.elsa_speak_clone.database.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;


import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private Context context;
    private EditText etNewUsername;
    private EditText etNewPassword;
    private EditText etRewritePassword;
    private Button btnRegisterUser;
    private ImageButton btnTogglePassword;
    private ImageButton btnToggleRewritePassword;
    private LinearLayout btnGoogleRegister;
    private TextView btnLogin;
    private TextView tvPassword;
    private GoogleSignInHelper googleSignInHelper;
    private TextView tvRewritePassword;
    private TextView tvUsername;
    private LearningAppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();
        db = new LearningAppDatabase(this);
        setupRegisterButton();
        setupLoginButton();
        setupShowPasswordButton(etNewPassword, btnTogglePassword);
        setupShowPasswordButton(etRewritePassword, btnToggleRewritePassword);
        initializeGoogleRegister();
        setupGoogleRegisterButton();

    }

    private void initializeViews() {
        etNewUsername = findViewById(R.id.etUsername);
        etNewPassword = findViewById(R.id.etPassword);
        etRewritePassword = findViewById(R.id.etRewritePassword);
        tvPassword = findViewById(R.id.tvPassword);
        tvRewritePassword = findViewById(R.id.tvRewritePassword);
        tvUsername = findViewById(R.id.tvUsername);
        btnRegisterUser = findViewById(R.id.btnRegisterUser);
        btnLogin = findViewById(R.id.btnLogin);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnToggleRewritePassword = findViewById(R.id.btnToggleLoginPassword);
        btnGoogleRegister = findViewById(R.id.btnGoogleRegister);
    }

    private void setupLoginButton() {
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupShowPasswordButton(EditText editText, ImageButton imageButton) {
        imageButton.setOnClickListener(new View.OnClickListener() {
            boolean isPasswordVisible = false;

            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Hide Password
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    imageButton.setImageResource(R.drawable.ic_eye_closed); // Change to closed eye icon
                    editText.setTypeface(null, Typeface.NORMAL); // Set to default font
                } else {
                    // Show Password
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    imageButton.setImageResource(R.drawable.ic_eye_open); // Change to open eye icon
                    editText.setTypeface(null, Typeface.NORMAL); // Set to default font
                }
                isPasswordVisible = !isPasswordVisible;
                editText.setSelection(editText.getText().length()); // Move cursor to the end
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GoogleSignInHelper.RC_SIGN_IN) {
            googleSignInHelper.handleActivityResult(requestCode, resultCode, data);
        }
    }

    private void setupGoogleRegisterButton() {
        btnGoogleRegister.setOnClickListener(v -> googleSignInHelper.signIn());
    }

    private void initializeGoogleRegister() {
        googleSignInHelper = new GoogleSignInHelper(this, new GoogleSignInHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Log.d("GoogleSignIn", "onSuccess called with user: " + (user != null ? user.getEmail() : "null"));

                if (user == null || user.getEmail() == null) {
                    Toast.makeText(RegisterActivity.this, "Failed to get user data", Toast.LENGTH_SHORT).show();
                    return;
                }

                String email = user.getEmail();
                String displayName = user.getDisplayName() != null ?
                        user.getDisplayName() : email.split("@")[0];

                // Check if user already exists
                int userId = db.getUserIdByEmail(email);

                if (userId != -1) {
                    Log.d("GoogleSignIn", "User exists with ID: " + userId);
                    Toast.makeText(RegisterActivity.this, "Account already exists, logging in...", Toast.LENGTH_SHORT).show();

                    // Create session for existing user
                    SessionManager sessionManager = new SessionManager(RegisterActivity.this);
                    sessionManager.createGoogleSession(displayName, userId);
                    navigateToMain();
                } else {
                    Log.d("GoogleSignIn", "Registering new user with email: " + email);
                    // New user - register
                    if (db.registerGoogleUser(email)) {
                        // Get the newly created user ID
                        userId = db.getUserIdByEmail(email);
                        if (userId != -1) {
                            // Create session
                            SessionManager sessionManager = new SessionManager(RegisterActivity.this);
                            sessionManager.createGoogleSession(displayName, userId);
                            Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Failed to create user account", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onError(String message) {
                Log.e("GoogleSignIn", "Error: " + message);
                Toast.makeText(RegisterActivity.this, "Google Sign-In Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    private void setupRegisterButton() {
        btnRegisterUser.setOnClickListener(v -> {
            String username = etNewUsername.getText().toString().trim();
            String password = etNewPassword.getText().toString().trim();
            String rewritePassword = etRewritePassword.getText().toString().trim();

            if (validateInput(username, password, rewritePassword)) {
                if (db.registerLocalUser(username, password)) {
                    // Create session after successful registration
                    SessionManager sessionManager = new SessionManager(RegisterActivity.this);
                    int userId = db.getUserId(username);
                    sessionManager.createSession(username, userId);

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
        else {
            hideTvUsername();
        }
        // Validate password and rewritePassword
        if (!validatePassword(password, rewritePassword)) {
            return false;
        }
        else {
            hideTvPassword();
            hideTvRewritePassword();
        }
       return true;
    }


    private boolean validateUsername(String username) {
        if (TextUtils.isEmpty(username)) {
            setTvUsername("Please enter a username.");
            return false;
        }

        if (username.length() < 6 || username.length() > 32) {
            setTvUsername("Username must be between 6 and 32 characters.");
            return false;
        }

        // Check for valid characters (letters, numbers, underscore, hyphen)
        if (!username.matches("^[a-zA-Z0-9_-]*$")) {
            setTvUsername("Username can only contain letters, numbers, underscore and hyphen.");
            return false;
        }

        // Must start with a letter
        if (!username.matches("^[a-zA-Z].*")) {
            setTvUsername("Username must start with a letter.");
            return false;
        }

        // Check for consecutive special characters
        if (username.contains("__") || username.contains("--") || username.contains("-_") || username.contains("_-")) {
            setTvUsername("Username cannot contain consecutive special characters.");
            return false;
        }

        if (!db.isUsernameAvailable(username)) {
            showToast("Username already exists.");
            return false;
        }

        return true;
    }

    private boolean validatePassword(String password, String rewritePassword) {
        if (TextUtils.isEmpty(password)) {
            setTvPassword("Please enter a password.");
            return false;
        }

        // Check for minimum requirements
        boolean hasUpperCase = password.matches(".*[A-Z].*");
        boolean hasLowerCase = password.matches(".*[a-z].*");
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()\\-_=+\\[\\]{};:'\",.<>/?].*");
        boolean hasMinLength = password.length() >= 8;

        // Build error message based on missing requirements
        StringBuilder errorMessage = new StringBuilder("Password must ");
        boolean hasError = false;

        if (!hasMinLength) {
            errorMessage.append("be at least 8 characters");
            hasError = true;
        }

        if (!hasNumber || !hasUpperCase || !hasLowerCase || !hasSpecialChar) {
            if (hasError) {
                errorMessage.append(" and ");
            }
            errorMessage.append("contain ");
            
            boolean isFirst = true;
            if (!hasUpperCase) {
                errorMessage.append("an uppercase letter");
                isFirst = false;
            }
            if (!hasLowerCase) {
                if (!isFirst) errorMessage.append(", ");
                errorMessage.append("a lowercase letter");
                isFirst = false;
            }
            if (!hasNumber) {
                if (!isFirst) errorMessage.append(", ");
                errorMessage.append("a number");
                isFirst = false;
            }
            if (!hasSpecialChar) {
                if (!isFirst) errorMessage.append(", ");
                errorMessage.append("a special character");
            }
            hasError = true;
        }

        if (hasError) {
            setTvPassword(errorMessage + ".");
            return false;
        }
        else {
            hideTvPassword();
        }

        if (!Objects.equals(password, rewritePassword)) {
            setTvRewritePassword();
            return false;
        }
        else {
            hideTvRewritePassword();
        }

        return true;
    }


    private void navigateToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    private void showToast(String message) {
        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    // Showing and hiding user input error messages
    private void setTvUsername(String message) {
        tvUsername.setText(message);
        tvUsername.setVisibility(View.VISIBLE);
    }

    private void hideTvUsername() {
        tvUsername.setText("");
        tvUsername.setVisibility(View.GONE);
    }

    private void setTvPassword(String message) {
        tvPassword.setText(message);
        tvPassword.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    private void setTvRewritePassword() {
        tvRewritePassword.setText("Password do not match");
        tvRewritePassword.setVisibility(View.VISIBLE);
    }

    private void hideTvPassword() {
        tvPassword.setText("");
        tvPassword.setVisibility(View.GONE);
    }

    private void hideTvRewritePassword() {
        tvRewritePassword.setText("");
        tvRewritePassword.setVisibility(View.GONE);
    }
   
}
