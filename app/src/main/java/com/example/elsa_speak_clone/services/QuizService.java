package com.example.elsa_speak_clone.services;

import android.content.Context;
import android.util.Log;

import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.entities.Quiz;
import com.example.elsa_speak_clone.database.entities.UserProgress;
import com.example.elsa_speak_clone.database.entities.UserScore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QuizService {
    private static final String TAG = "QuizService";
    private final AppDatabase database;
    private final Context context;

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
     * Add XP points to user score for a specific lesson
     * 
     * @param userId The user ID
     * @param lessonId The lesson ID
     * @param points The number of points to add
     */
    public void addXpPoints(int userId, int lessonId, int points) {
        try {
            // Check if score entry exists
            UserScore existingScore = database.userScoreDao().getUserScoreByLessonAndUser(userId, lessonId);
            
            if (existingScore != null) {
                // Update existing score
                existingScore.setScore(existingScore.getScore() + points);
                database.userScoreDao().update(existingScore);
            } else {
                // Create new score entry
                UserScore newScore = new UserScore();
                newScore.setUserId(userId);
                newScore.setLessonId(lessonId);
                newScore.setScore(points);
                newScore.setDateUpdated(getCurrentDate());
                database.userScoreDao().insert(newScore);
            }
            
            Log.d(TAG, "Added " + points + " XP for user " + userId + " in lesson " + lessonId);
        } catch (Exception e) {
            Log.e(TAG, "Error adding XP points: " + e.getMessage(), e);
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