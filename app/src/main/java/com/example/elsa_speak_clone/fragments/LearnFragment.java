package com.example.elsa_speak_clone.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elsa_speak_clone.activities.QuizActivity;
import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.entities.Lesson;
import com.example.elsa_speak_clone.classes.LessonAdapter;
import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.activities.SpeechToText;
import com.example.elsa_speak_clone.database.DataInitializer;
import com.example.elsa_speak_clone.services.NavigationService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LearnFragment extends Fragment {

    private final String TAG = "LearnFragment";

    private RecyclerView recyclerLessons;
    private ProgressBar progressBar;
    private AppDatabase database;
    private ExecutorService executor;
    private Handler mainHandler;
    private NavigationService navigationService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_learn, container, false);

        initialize();
        // Load lessons from database
        loadLessons();
        
        return view;
    }

    private void initialize() {
        // Initialize views
        recyclerLessons = view.findViewById(R.id.recyclerLessons);
        navigationService = new NavigationService(requireContext());

        // Initialize database and thread components
        database = AppDatabase.getInstance(requireContext());
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }
    private void loadLessons() {
        // Show loading indicator
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        executor.execute(() -> {
            try {
                // Use Room DAO to get all lessons
                List<Lesson> lessonList = database.lessonDao().getAllLessons();
                Log.d(TAG, "Loaded " + lessonList.size() + " lessons from database");

                try {
if (lessonList.size() < 3 ) {
                    forceDataUpdate();
                }
                 } catch (Exception e) { 

                }
                

                // Update UI on main thread
                mainHandler.post(() -> {
                    if (isAdded()) { // Check if fragment is still attachedf
                        setupRecyclerView(lessonList);
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading lessons", e);
                
                mainHandler.post(() -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), 
                                "Error loading lessons: " + e.getMessage(), 
                                Toast.LENGTH_LONG).show();
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }

    private void setupRecyclerView(List<Lesson> lessonList) {
        if (lessonList.isEmpty()) {
            // Handle empty state - perhaps show a message
            Toast.makeText(requireContext(), "No lessons available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        LessonAdapter adapter = new LessonAdapter(lessonList, database, lesson -> {
            // Handle lesson click (navigate to detail fragment/activity)
            Toast.makeText(requireContext(), "Selected: " + lesson.getTopic(), Toast.LENGTH_SHORT).show();

            navigationService.navigateToQuiz(lesson.getLessonId());
        });

        // Set up RecyclerView
        recyclerLessons.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerLessons.setAdapter(adapter);
    }

    // Don't know why somehow database doesn't auto update so force it instead.
    private void forceDataUpdate() {
        executor.execute(() -> {
            try {
                // Force update data
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    try {
                        DataInitializer.updateData(database);
                        Log.d(TAG, "Forced data update completed");
                        
                        // Reload lessons after update
                        mainHandler.post(this::loadLessons);
                    } catch (Exception e) {
                        Log.e(TAG, "Error during forced data update", e);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to execute data update", e);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up resources
        if (executor != null) {
            executor.shutdown();
        }
    }
}