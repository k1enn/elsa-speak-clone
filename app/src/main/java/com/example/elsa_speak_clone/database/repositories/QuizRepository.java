package com.example.elsa_speak_clone.database.repositories;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.dao.QuizDao;
import com.example.elsa_speak_clone.database.entities.Quiz;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Repository class for handling Quiz-related database operations
 */
public class QuizRepository {
    private static final String TAG = "QuizRepository";
    private final QuizDao quizDao;
    private final AppDatabase database;
    
    // LiveData properties for observing quiz data
    private final MutableLiveData<List<Quiz>> allQuizzes = new MutableLiveData<>();
    private final MutableLiveData<List<Quiz>> lessonQuizzes = new MutableLiveData<>();
    private final MutableLiveData<Quiz> currentQuiz = new MutableLiveData<>();

    public QuizRepository(Context context) {
        database = AppDatabase.getInstance(context);
        quizDao = database.quizDao();
    }

    public LiveData<List<Quiz>> getAllQuizzes() {
        refreshAllQuizzes();
        return allQuizzes;
    }

    public LiveData<List<Quiz>> getQuizzesForLesson(int lessonId) {
        refreshLessonQuizzes(lessonId);
        return lessonQuizzes;
    }

    public LiveData<Quiz> getCurrentQuiz() {
        return currentQuiz;
    }

    private void refreshAllQuizzes() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Quiz> quizzes = quizDao.getAllQuizzes();
            allQuizzes.postValue(quizzes);
        });
    }

    private void refreshLessonQuizzes(int lessonId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Quiz> quizzes = quizDao.getQuizzesForLesson(lessonId);
            lessonQuizzes.postValue(quizzes);
        });
    }

    public List<Quiz> getAllQuizzesSync() {
        try {
            Future<List<Quiz>> future = AppDatabase.databaseWriteExecutor.submit(quizDao::getAllQuizzes);
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting all quizzes", e);
            return new ArrayList<>();
        }
    }

    public List<Quiz> getQuizzesForLessonSync(int lessonId) {
        try {
            Future<List<Quiz>> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                quizDao.getQuizzesForLesson(lessonId));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting quizzes for lesson " + lessonId, e);
            return new ArrayList<>();
        }
    }

    public Quiz getQuizById(int quizId) {
        try {
            Future<Quiz> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                quizDao.getQuizById(quizId));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting quiz " + quizId, e);
            return null;
        }
    }

    public Quiz getRandomQuizForLesson(int lessonId) {
        try {
            Future<Quiz> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                quizDao.getRandomQuizForLesson(lessonId));
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting random quiz for lesson " + lessonId, e);
            return null;
        }
    }

    public int countQuizzesForLesson(int lessonId) {
        try {
            Future<List<Quiz>> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                quizDao.getQuizzesForLesson(lessonId));
            List<Quiz> quizzes = future.get();
            return quizzes != null ? quizzes.size() : 0;
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error counting quizzes for lesson " + lessonId, e);
            return 0;
        }
    }


} 