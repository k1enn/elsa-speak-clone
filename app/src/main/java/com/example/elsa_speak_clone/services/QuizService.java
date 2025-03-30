package com.example.elsa_speak_clone.services;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.dao.UserProgressDao;
import com.example.elsa_speak_clone.database.entities.Quiz;
import com.example.elsa_speak_clone.database.entities.UserProgress;
import com.example.elsa_speak_clone.database.repositories.UserProgressRepository;
import com.example.elsa_speak_clone.database.repositories.UserRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuizService {
    private static final String TAG = "QuizService";
    private final AppDatabase database;
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public QuizService(Context context) {
        this.context = context;
        this.database = AppDatabase.getInstance(context);
    }

    /**
     * Get a random quiz for a specific lesson
     * 
     * @param lessonId The lesson ID
     * @return A random quiz for the lesson
     */
    public Quiz getRandomQuizForLesson(int lessonId) {
        return database.quizDao().getRandomQuizForLesson(lessonId);
    }

    /**
     * Add XP points and sync to Firebase
     */
    public void addXpPoints(int userId, int lessonId, int points) {
        try {
            // Check if score entry exists for this specific user AND lesson combination
            UserProgressRepository userProgressRepository = new UserProgressRepository(this.context);
            UserProgress existingProgress = database.userProgressDao().getUserLessonProgress(userId, lessonId);
            UserProgressDao userProgressDao = userProgressRepository.getUserProgressDao();


            if (existingProgress != null) {
                // Update existing progress for this lesson
//                existingProgress.setXp(existingProgress.getXp() + points);
                existingProgress.setLastStudyDate(getCurrentDate());
                database.userProgressDao().update(existingProgress);
                userProgressDao.updateXpPoints(userId, points);
                Log.d(TAG, "Updated XP for existing progress: " + existingProgress.getProgressId() +
                        "\n Total XP: " + existingProgress.getXp());
                
            } else {
                // Create new progress entry
                // First, determine the next available progress ID
                int progressId = getNextProgressId(userId);
                
                // Create new progress with all required fields
                UserProgress newProgress = new UserProgress(
                    progressId,
                    userId,
                    lessonId,
                    0, // Default difficulty level
                    null, // No completion time yet
                    1, // Start with streak of 1
                    points, // Initial XP points
                    getCurrentDate() // Current date
                );
                
                // Insert the new progress
                long insertedId = database.userProgressDao().insert(newProgress);
                Log.d(TAG, "Created new progress with ID: " + insertedId);
                
            }
            
            Log.d(TAG, "Added " + points + " XP for user " + userId + " in lesson " + lessonId);
        } catch (Exception e) {
            Log.e(TAG, "Error adding XP points: " + e.getMessage(), e);
        }
    }


    /**
     * Marks a lesson as completed
     * @param userId The user ID
     * @param lessonId The lesson ID
     */
    private void markLessonCompleted(int userId, int lessonId) {
        try {
            // Get current user progress
            UserProgress progress = database.userProgressDao().getUserLessonProgress(userId, lessonId);
            
            if (progress != null) {
                // Set completion time if not already set
                if (progress.getCompletionTime() == null) {
                    progress.setCompletionTime(getCurrentTime());
                    database.userProgressDao().update(progress);
                    Log.d(TAG, "Marked lesson " + lessonId + " as completed for user " + userId);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error marking lesson as completed: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the current time formatted as a string
     */
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Get next available progress ID for a user
     * 
     * @param userId The user ID
     * @return The next available progress ID
     */
    private int getNextProgressId(int userId) {
        try {
            // Count existing progress entries and add 1
            int count = database.userProgressDao().countUserProgressEntries(userId);
            return (userId * 1000) + count + 1; // This creates unique IDs like 1001, 1002, etc. for user 1
        } catch (Exception e) {
            Log.e(TAG, "Error getting next progress ID", e);
            // Fallback to a timestamp-based ID if query fails
            return (userId * 1000) + (int)(System.currentTimeMillis() % 1000);
        }
    }

    /**
     * Get the current date as a string in yyyy-MM-dd format
     * 
     * @return The current date string
     */
    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }
} 