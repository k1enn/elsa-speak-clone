package com.example.elsa_speak_clone.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.adapters.LeaderboardAdapter;
import com.example.elsa_speak_clone.database.firebase.FirebaseDataManager;
import com.example.elsa_speak_clone.models.LeaderboardEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

public class LeaderboardActivity extends AppCompatActivity {
    private static final String TAG = "LeaderboardActivity";
    private static final int LEADERBOARD_LIMIT = 20; // Top 20 users
    private static final String LEADERBOARD_PATH = "leaderboard";
    
    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    
    private FirebaseDataManager firebaseDataManager;
    private DatabaseReference leaderboardRef;
    private ValueEventListener leaderboardListener;
    
    private String currentSortMethod = "xp"; // Default sort by XP
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        initialize();

        // Setup real-time data listener
        setupLeaderboardListener();
    }

    private void initialize() {
        // Initialize views
        setupToolbar ();
        recyclerView = findViewById(R.id.recyclerLeaderboard);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        // Initialize Firebase manager and reference
        firebaseDataManager = FirebaseDataManager.getInstance(this);
        leaderboardRef = FirebaseDatabase.getInstance().getReference(LEADERBOARD_PATH);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    private void setupLeaderboardListener() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Create a value event listener for real-time updates
        leaderboardListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);
                
                if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount() == 0) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    return;
                }
                
                tvEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                
                // Process data and update UI
                processLeaderboardData(dataSnapshot);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading leaderboard: " + databaseError.getMessage());
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText(R.string.error_loading_leaderboard);
            }
        };
        
        // Attach the listener to get top users by XP
        leaderboardRef.orderByChild("userXp")
                .limitToLast(LEADERBOARD_LIMIT)
                .addValueEventListener(leaderboardListener);
    }
    
    private void processLeaderboardData(DataSnapshot dataSnapshot) {
        Map<String, Map<String, Object>> usersMap = new HashMap<>();
        
        // Convert DataSnapshot to map
        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
            String username = userSnapshot.getKey();
            Map<String, Object> userData = new HashMap<>();
            
            for (DataSnapshot field : userSnapshot.getChildren()) {
                userData.put(field.getKey(), field.getValue());
            }
            
            usersMap.put(username, userData);
        }
        
        // Convert map to list for adapter
        List<LeaderboardEntry> entries = new ArrayList<>();
        
        for (Map.Entry<String, Map<String, Object>> entry : usersMap.entrySet()) {
            String username = entry.getKey();
            Map<String, Object> userData = entry.getValue();
            
            // Safely convert values from Firebase (handles both Integer and Long)
            int userId = 0;
            int streak = 0;
            int xp = 0;
            
            if (userData.get("userId") != null) {
                userId = ((Number) userData.get("userId")).intValue();
            }
            
            if (userData.get("userStreak") != null) {
                streak = ((Number) userData.get("userStreak")).intValue();
            }
            
            if (userData.get("userXp") != null) {
                xp = ((Number) userData.get("userXp")).intValue();
            }
            
            entries.add(new LeaderboardEntry(
                    username, 
                    userId, 
                    streak, 
                    xp
            ));
        }
        
        // Sort by XP (highest first)
        Collections.sort(entries, (e1, e2) -> Integer.compare(e2.getXp(), e1.getXp()));
        
        // Apply current sort method after processing data
        if (currentSortMethod.equals("xp")) {
            sortLeaderboardByXp();
        } else {
            sortLeaderboardByStreak();
        }
        
        // Update adapter
        adapter.updateData(entries);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_sort) {
            showSortMenu(item.getActionView() != null ? item.getActionView() : findViewById(R.id.action_sort));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_leaderboard, menu);
        return true;
    }

    private void showSortMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.menu_leaderboard_sort, popup.getMenu());
        
        // Set checkmark on current sort method
        if (currentSortMethod.equals("xp")) {
            popup.getMenu().findItem(R.id.sort_by_xp).setChecked(true);
        } else {
            popup.getMenu().findItem(R.id.sort_by_streak).setChecked(true);
        }
        
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.sort_by_xp) {
                currentSortMethod = "xp";
                sortLeaderboardByXp();
                return true;
            } else if (id == R.id.sort_by_streak) {
                currentSortMethod = "streak";
                sortLeaderboardByStreak();
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    private void sortLeaderboardByXp() {
        List<LeaderboardEntry> entries = adapter.getEntries();
        Collections.sort(entries, (e1, e2) -> Integer.compare(e2.getXp(), e1.getXp()));
        adapter.updateData(entries);
    }

    private void sortLeaderboardByStreak() {
        List<LeaderboardEntry> entries = adapter.getEntries();
        Collections.sort(entries, (e1, e2) -> Integer.compare(e2.getStreak(), e1.getStreak()));
        adapter.updateData(entries);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the listener when the activity is destroyed to prevent memory leaks
        if (leaderboardRef != null && leaderboardListener != null) {
            leaderboardRef.removeEventListener(leaderboardListener);
        }
    }
} 