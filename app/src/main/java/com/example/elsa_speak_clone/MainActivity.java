
package com.example.elsa_speak_clone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.elsa_speak_clone.HomeFragment;
import com.example.elsa_speak_clone.ProfileFragment;
import com.example.elsa_speak_clone.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    Context context;
    SharedPreferences prefs;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeVariable();
        initializeSharedPreferences();

        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        loadFragment(new HomeFragment());

    }
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
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
    private void initializeVariable() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(navListener);
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
