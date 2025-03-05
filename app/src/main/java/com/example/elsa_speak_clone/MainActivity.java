
package com.example.elsa_speak_clone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.elsa_speak_clone.HomeFragment;
import com.example.elsa_speak_clone.ProfileFragment;
import com.example.elsa_speak_clone.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private int currentPosition = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initializeVariable();
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
            loadFragment(new HomeFragment(), currentPosition);
        }
    }
    private void initializeVariable() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(navListener);
    }

    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        Fragment selectedFragment;
        int newPosition;

        int itemId = item.getItemId();

        if (itemId == R.id.nav_learn) {
            selectedFragment = new LearnFragment();
            newPosition = 0;
        } else if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
            newPosition = 1;
        } else if (itemId == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
            newPosition = 2;
        } else {
            return false;
        }

        loadFragment(selectedFragment, newPosition);
        return true;
    };

    private void loadFragment(Fragment fragment, int newPosition) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (newPosition > currentPosition) {
            // Swipe left animation (new fragment enters from right)
            transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left

            );
        } else if (newPosition < currentPosition) {
            // Swipe right animation (new fragment enters from left)
            transaction.setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            );
        }
        
        transaction.replace(R.id.fragment_container, fragment).commit();
        
        currentPosition = newPosition; // update position after loading fragment
    }
}
