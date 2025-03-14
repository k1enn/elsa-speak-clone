
package com.example.elsa_speak_clone.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.database.SessionManager;
import com.example.elsa_speak_clone.fragments.HomeFragment;
import com.example.elsa_speak_clone.fragments.LearnFragment;
import com.example.elsa_speak_clone.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    SharedPreferences prefs;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeVariable();
        initializeSharedPreferences();
        checkUserLogin();

        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        loadFragment(new HomeFragment());

    }


    private void initializeSharedPreferences() {
        String username = getIntent().getStringExtra("username");
        int userId = getIntent().getIntExtra("userId", -1);

        if (username != null && userId != -1) {
            // Save to SharedPreferences here
            prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            prefs.edit().putString("username", username).putInt("userId", userId).apply();
        }

    }

    private void checkUserLogin() {

        // Check if user is already logged in
        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            // User is not logged in, go to login activity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }
    private void initializeVariable() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(navListener);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            // Show a confirmation dialog
            finishAffinity(); // Close all activities
            System.exit(0); // Exit the app
        }
    }

    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        Fragment selectedFragment;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_learn) {
            selectedFragment = new LearnFragment();
        } else if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (itemId == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        } else {
            return false;
        }

        loadFragment(selectedFragment);
        return true;
    };

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment).commit();
    }
}
