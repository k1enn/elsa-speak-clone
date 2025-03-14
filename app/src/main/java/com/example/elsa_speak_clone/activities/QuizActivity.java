package com.example.elsa_speak_clone.activities;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.media.MediaPlayer;

import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.database.LearningAppDatabase;
import com.example.elsa_speak_clone.database.SessionManager;

import java.util.ArrayList;
import java.util.List;


public class QuizActivity extends AppCompatActivity {
    private LearningAppDatabase dbHelper;
    private TextView tvQuestion, tvResult;
    private EditText etAnswer;
    private Button btnCheckAnswer, btnNextQuestion;
    private String correctAnswer;
    private int currentLessonId = 1; // Default to lesson 1
    private List<Integer> usedQuizIds = new ArrayList<>(); // Track used questions
    private MediaPlayer correctSoundPlayer; // For the "ting ting" sound


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        initialize();
        loadNextQuestion();

        btnCheckAnswer.setOnClickListener(v -> checkAnswer());
        btnNextQuestion.setOnClickListener(v -> loadNextQuestion());
    }

    private void initialize() {
        // Get the lesson ID from the intent
        if (getIntent().hasExtra("LESSON_ID")) {
            currentLessonId = getIntent().getIntExtra("LESSON_ID", 1);
        }
        dbHelper = new LearningAppDatabase(this);
        tvQuestion = findViewById(R.id.tvQuestion);
        etAnswer = findViewById(R.id.etAnswer);
        btnCheckAnswer = findViewById(R.id.btnCheckAnswer);
        btnNextQuestion = findViewById(R.id.btnNextQuestion);
        tvResult = findViewById(R.id.tvResult);

        // Initialize the sound player
        correctSoundPlayer = MediaPlayer.create(this, R.raw.correct_sound);
    }
    private void loadNextQuestion() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Modified query to exclude already shown questions
        String query = "SELECT QuizId, Question, Answer FROM Quizzes WHERE LessonId = ? AND QuizId NOT IN (";

        // Create placeholders for used quiz IDs
        if (!usedQuizIds.isEmpty()) {
            for (int i = 0; i < usedQuizIds.size(); i++) {
                query += "?";
                if (i < usedQuizIds.size() - 1) {
                    query += ",";
                }
            }
            query += ") ORDER BY RANDOM() LIMIT 1";
            // SELECT QuizId, Question, Answer FROM Quizzes WHERE LessonId = ?
            // AND QuizId NOT IN (?,?,?) ORDER BY RANDOM() LIMIT 1

            // Create the arguments array with lesson ID and used quiz IDs
            String[] args = new String[usedQuizIds.size() + 1];
            args[0] = String.valueOf(currentLessonId);
            for (int i = 0; i < usedQuizIds.size(); i++) {
                args[i + 1] = String.valueOf(usedQuizIds.get(i));
            }

            Cursor cursor = db.rawQuery(query, args);
            handleCursorResult(cursor);
        } else {
            // First question, no used IDs yet
            Cursor cursor = db.rawQuery(
                    "SELECT QuizId, Question, Answer FROM Quizzes WHERE LessonId = ? ORDER BY RANDOM() LIMIT 1",
                    new String[]{String.valueOf(currentLessonId)}
            );
            handleCursorResult(cursor);
        }

        db.close();
    }

    private void handleCursorResult(Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            int quizId = cursor.getInt(0);
            String question = cursor.getString(1);
            correctAnswer = cursor.getString(2).trim();

            // Add this question to used questions
            usedQuizIds.add(quizId);

            tvQuestion.setText(question);
            etAnswer.setText(""); // Clear previous input
            tvResult.setText(""); // Clear previous result
            btnNextQuestion.setVisibility(Button.GONE);
        } else {
            // No more unused questions for this lesson
            if (!usedQuizIds.isEmpty()) {
                // All questions have been used, show congratulations popup
                showCongratulationsPopup();
                // Reset the list to start over if they want to try again
                usedQuizIds.clear();
            } else {
                // No questions at all for this lesson
                tvQuestion.setText("No questions available for this lesson!");
                correctAnswer = "";
            }
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private void showCongratulationsPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_congratulations, null);

        // Create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = false; // Not allows taps outside the popup to dismiss it

        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // Set a nice animation for the popup
        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);

        // Show the popup centered in the screen
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);

        // Set up the close button
        Button btnClose = popupView.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> {
            popupWindow.dismiss();
            // finish();
        });

        // Add a dim background effect
        View rootView = getWindow().getDecorView().getRootView();
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) rootView.getLayoutParams();
        params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        params.dimAmount = 0.5f;
        getWindow().setAttributes(params);

        // Reset the dim effect when popup is dismissed
        popupWindow.setOnDismissListener(() -> {
            params.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            getWindow().setAttributes(params);
        });
    }


    private void checkAnswer() {
        String userAnswer = etAnswer.getText().toString().trim().replaceAll("\\s+", " ");

        if (userAnswer.equalsIgnoreCase(correctAnswer.replaceAll("\\s+", " "))) {
            tvResult.setText("✅ Correct!");
            tvResult.setTextColor(Color.GREEN);

            // Play the "ting ting" sound
            if (correctSoundPlayer != null) {
                correctSoundPlayer.start();
            }

            // Optional: Update user score in database
            SessionManager sessionManager = new SessionManager(this);
            int userId = Integer.parseInt(sessionManager.getUserDetails().get("userId"));
            // Get the quiz ID (you would need to store this when loading the question)
            // dbHelper.addQuizScore(userId, quizId, 1);
        } else {
            tvResult.setText("❌ Incorrect! The correct answer is: " + correctAnswer);
            tvResult.setTextColor(Color.RED);
        }

        btnNextQuestion.setVisibility(Button.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release MediaPlayer resources
        if (correctSoundPlayer != null) {
            correctSoundPlayer.release();
            correctSoundPlayer = null;
        }
    }
}
