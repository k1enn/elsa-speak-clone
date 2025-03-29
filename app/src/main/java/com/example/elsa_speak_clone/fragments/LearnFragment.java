package com.example.elsa_speak_clone.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.entities.Lesson;
import com.example.elsa_speak_clone.database.entities.Quiz;
import com.example.elsa_speak_clone.adapters.LessonAdapter;
import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.database.DataInitializer;
import com.example.elsa_speak_clone.database.repositories.QuizRepository;
import com.example.elsa_speak_clone.services.NavigationService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LearnFragment extends Fragment {

    private final String TAG = "LearnFragment";
    private RecyclerView recyclerLessons;

    // Background thread
    private ExecutorService executor;
    private Handler mainHandler;

    // Navigate
    private NavigationService navigationService;

    // Data
    private QuizRepository quizRepository;
    private AppDatabase database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_learn, container, false);

        initializeUI(view);
        initializeDatabase();
        initializeNavigationService();
        loadLessons();

        return view;
    }

    private void initializeUI(View view) {
        try {
            recyclerLessons = view.findViewById(R.id.recyclerLessons);
            recyclerLessons = view.findViewById(R.id.recyclerLessons);
            recyclerLessons.setLayoutManager(new LinearLayoutManager(getContext()));

            Log.d(TAG, "Successfully initialize RecyclerView");
        } catch (Exception e) {
            Log.d(TAG, "Error when initialize RecyclerView");
        }
        // Background run
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    private void initializeNavigationService() {
        navigationService = new NavigationService(requireContext());
    }

    private void initializeDatabase() {
        try {
            database = AppDatabase.getInstance(requireContext());
            quizRepository = new QuizRepository(requireContext());

            Log.d(TAG, "Successfully initialize database");
        } catch (Exception e) {
            Log.d(TAG, "Error in initialize database");
        }
    }
    private void loadLessons() {
        executor.execute(() -> {
            try {
                List<Lesson> lessonList = database.lessonDao().getAllLessons();
                Log.d(TAG, "Loaded " + lessonList.size() + " lessons from database");

                for (Lesson lesson : lessonList) {
                    Log.d(TAG, "Lesson: " + lesson.getLessonId() + " - " + lesson.getTopic());

                    int quizCount = quizRepository.countQuizzesForLesson(lesson.getLessonId());
                    Log.d(TAG, "Lesson " + lesson.getLessonId() + " has " + quizCount + " quizzes");
                }

                // If some load thing went wrong, load again for sure
                if (lessonList.size() < 2) {
                    try {
                        forceDataUpdate();
                    } catch (Exception e) {
                        Log.d(TAG, "Can not force update lessons");
                    }
                }

                mainHandler.post(() -> {
                    if (isAdded()) {
                        setupRecyclerView(lessonList);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading lessons", e);

                mainHandler.post(() -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(),
                                "Error loading lessons: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();

                    }
                });
            }
        });
    }


    private void setupRecyclerView(List<Lesson> lessonList) {
        if (lessonList.isEmpty()) {
            Toast.makeText(requireContext(), "No lessons available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create adapter
        LessonAdapter adapter = new LessonAdapter(lessonList, database, lesson -> executor.execute(() -> {
            try {
                List<Quiz> quizzes = quizRepository.getQuizzesForLessonSync(lesson.getLessonId());
                Log.d(TAG, "Loading " + quizzes.size() + " quizzes for lesson " + lesson.getLessonId());

                mainHandler.post(() -> navigationService.navigateToSpeechToText(lesson.getLessonId()));
            } catch (Exception e) {
                Log.e(TAG, "Error loading quizzes " + lesson.getLessonId(), e);
                mainHandler.post(() -> navigationService.navigateToQuiz(lesson.getLessonId()));
            }
        }));

        recyclerLessons.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerLessons.setAdapter(adapter);
    }

    // Sometime can't load for the first time
    private void forceDataUpdate() {
        executor.execute(() -> {
            try {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    try {
                        DataInitializer.updateData(database);
                        Log.d(TAG, "Update completed");

                        List<Quiz> allQuizzes = quizRepository.getAllQuizzesSync();
                        Log.d(TAG, "Total quizzes after update: " + allQuizzes.size());

                        mainHandler.post(this::loadLessons);
                    } catch (Exception e) {
                        Log.e(TAG, "Error in update lessons", e);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to update data", e);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh lessons when returning to learn fragment after done something
        refreshLessons();
    }

    // Add new method if data changed to refresh without full reload
    @SuppressLint("NotifyDataSetChanged")
    private void refreshLessons() {
        if (recyclerLessons != null && recyclerLessons.getAdapter() != null) {
            // Trigger adapter refresh
            recyclerLessons.getAdapter().notifyDataSetChanged();

            // Log the refresh
            Log.d(TAG, "Refreshed lesson display to show progress updates");
        }
    }
}