package com.example.elsa_speak_clone.activities;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.media.MediaPlayer;

import com.airbnb.lottie.LottieAnimationView;
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

    LottieAnimationView lottieConfetti;
    private int currentLessonId = 1; // Default to lesson 1
    private List<Integer> usedQuizIds = new ArrayList<>(); // Track used questions
    private MediaPlayer correctSoundPlayer; // For the "ting ting" sound


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        initialize();
        loadNextQuestion();

        returnCheckAnswerButton();
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
        // Initialize the confetti animation view
        lottieConfetti = findViewById(R.id.lottieConfetti);
        // Initialize the sound player
        correctSoundPlayer = MediaPlayer.create(this, R.raw.correct_sound);
    }
    private void loadNextQuestion() {
        returnCheckAnswerButton();
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

        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = false;

        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);

        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);

        // Call the separate method to display the confetti popup ABOVE this one
        showConfettiPopup();

        Button btnClose = popupView.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> {
            popupWindow.dismiss();
            navigatetoMain();

            // Ensure we dismiss the confetti if it's still visible
            if (confettiPopup != null && confettiPopup.isShowing()) {
                confettiPopup.dismiss();
            }
        });

        View rootView = getWindow().getDecorView().getRootView();
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) rootView.getLayoutParams();

        params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        params.dimAmount = 0.5f;

        getWindow().setAttributes(params);

        popupWindow.setOnDismissListener(() -> {
            params.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            getWindow().setAttributes(params);

            // Dismiss the confetti when popup is dismissed
            if (confettiPopup != null && confettiPopup.isShowing()) {
                confettiPopup.dismiss();
            }
        });
    }


    private PopupWindow confettiPopup;

    private void showConfettiPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Create a simple FrameLayout to hold the LottieAnimationView
        FrameLayout confettiLayout = new FrameLayout(this);
        confettiLayout.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // Create and configure the LottieAnimationView
        LottieAnimationView confettiView = new LottieAnimationView(this);
        confettiView.setAnimation(R.raw.confetti);
        confettiView.setScaleX(4.0f); // 400% bigger
        confettiView.setScaleY(4.0f);
        confettiView.setRepeatCount(0); // Play once
        confettiView.playAnimation();

        // Add the LottieAnimationView to the layout
        confettiLayout.addView(confettiView);

        // Create the PopupWindow with transparent background
        confettiPopup = new PopupWindow(
                confettiLayout,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                false);

        // Transparent background for popup
        confettiPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Show it on top of everything else
        confettiPopup.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0);

        // Automatically dismiss after animation ends
        confettiView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animator) { }
            @Override public void onAnimationEnd(Animator animator) {
                if (confettiPopup != null && confettiPopup.isShowing()) {
                    confettiPopup.dismiss();
                }
            }
            @Override public void onAnimationCancel(Animator animator) { }
            @Override public void onAnimationRepeat(Animator animator) { }
        });
    }


    private void checkAnswer() {
        String userAnswer = etAnswer.getText().toString().trim().replaceAll("\\s+", " ");

        // If user correct
        if (userAnswer.equalsIgnoreCase(correctAnswer.replaceAll("\\s+", " "))) {
            tvResult.setText("✅ Correct!");
            tvResult.setTextColor(Color.GREEN);

            // Play the "ting ting" sound
            try {
                correctSoundPlayer.start();
            } catch (NullPointerException e) {
                Log.d("QuizActivity", "Correct sound is NULL");
            }
            SessionManager sessionManager = new SessionManager(this);
            int userId = Integer.parseInt(sessionManager.getUserDetails().get("userId"));

            // Update user score in database by difficulty
            if (currentLessonId > 5) {
                dbHelper.addXpPoints(userId, currentLessonId, 10);
            } else {
                dbHelper.addXpPoints(userId, currentLessonId, 5);
            }
            dbHelper.updateUserStreak(this);

            changeCheckAnswerButton();

        } else {
            tvResult.setText("❌ Incorrect! The correct answer is: " + correctAnswer);
            tvResult.setTextColor(Color.RED);
        }

    }
    private void changeCheckAnswerButton() {
        btnCheckAnswer.setText("Tiếp tục");
        btnCheckAnswer.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_button_green));
        btnCheckAnswer.setOnClickListener(v -> loadNextQuestion());
    }

    private void returnCheckAnswerButton() {
        btnCheckAnswer.setText("Kiểm tra");
        btnCheckAnswer.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_button));
        btnCheckAnswer.setOnClickListener(v ->{
            checkAnswer();
        });
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
    @Override
    public void onBackPressed() {

        // Create intent to navigate to MainActivity
        super.onBackPressed();

        navigatetoMain();
        // Update user streak when returning to MainActivity
        dbHelper.updateUserStreak(this);
    }

    private void navigatetoMain() {
        Intent intent = new Intent(this, MainActivity.class);

        // Add flags to properly handle the back stack
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Start MainActivity
        startActivity(intent);
        finish();
    }

}
