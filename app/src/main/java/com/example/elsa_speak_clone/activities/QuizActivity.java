package com.example.elsa_speak_clone.activities;

import android.animation.Animator;
import android.content.Context;
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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.airbnb.lottie.LottieAnimationView;
import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.SessionManager;
import com.example.elsa_speak_clone.database.entities.Quiz;
import com.example.elsa_speak_clone.database.entities.UserScore;
import com.example.elsa_speak_clone.services.NavigationService;
import com.example.elsa_speak_clone.services.QuizService;
import com.example.elsa_speak_clone.database.firebase.FirebaseDataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuizActivity extends AppCompatActivity {
    private static final String TAG = "QuizActivity";
    
    // Services
    private NavigationService navigationService;
    private QuizService quizService;
    private SessionManager sessionManager;
    private AppDatabase database;
    private FirebaseDataManager firebaseDataManager;
    
    // UI components
    private TextView tvQuestion, tvResult;
    private EditText etAnswer;
    private Button btnCheckAnswer, btnNextQuestion;
    private LottieAnimationView lottieConfetti;

    // Quiz data
    private String correctAnswer;
    private int currentLessonId = 1; // Default to lesson 1
    private List<Integer> usedQuizIds = new ArrayList<>(); // Track used questions
    private MediaPlayer correctSoundPlayer; // For the "ting ting" sound
    private PopupWindow confettiPopup;
    
    // Thread management
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        initializeServices();
        initializeUI();
        loadNextQuestion();
        firebaseDataManager = FirebaseDataManager.getInstance(this);
    }
    
    private void initializeServices() {
        navigationService = new NavigationService(this);
        sessionManager = new SessionManager(this);
        database = AppDatabase.getInstance(this);
        quizService = new QuizService(this);
    }

    private void initializeUI() {
        // Get the lesson ID from the intent
        if (getIntent().hasExtra("LESSON_ID")) {
            currentLessonId = getIntent().getIntExtra("LESSON_ID", 1);
        }
        
        tvQuestion = findViewById(R.id.tvQuestion);
        etAnswer = findViewById(R.id.etAnswer);
        btnCheckAnswer = findViewById(R.id.btnCheckAnswer);
        btnNextQuestion = findViewById(R.id.btnNextQuestion);
        tvResult = findViewById(R.id.tvResult);
        
        // Initialize the confetti animation view
        lottieConfetti = findViewById(R.id.lottieConfetti);
        
        // Initialize the sound player
        correctSoundPlayer = MediaPlayer.create(this, R.raw.correct_sound);
        
        // Setup check answer button initial state
        returnCheckAnswerButton();
    }
    
    private void loadNextQuestion() {
        returnCheckAnswerButton();
        
        executor.execute(() -> {
            try {
                // Get random quiz for current lesson that hasn't been used yet
                Quiz quiz = null;
                
                if (usedQuizIds.isEmpty()) {
                    // First question, no used IDs yet
                    quiz = database.quizDao().getRandomQuizForLesson(currentLessonId);
                } else {
                    // Get a quiz not in used list
                    quiz = database.quizDao().getRandomQuizForLessonExcluding(currentLessonId, usedQuizIds);
                }
                
                final Quiz finalQuiz = quiz;
                
                runOnUiThread(() -> {
                    if (finalQuiz != null) {
                        // Add this question to used questions
                        usedQuizIds.add(finalQuiz.getQuizId());
                        
                        tvQuestion.setText(finalQuiz.getQuestion());
                        correctAnswer = finalQuiz.getAnswer().trim();
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
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading quiz: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    tvQuestion.setText("Error loading quiz. Please try again.");
                });
            }
        });
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
            navigationService.navigateToMain();

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
            } catch (Exception e) {
                Log.e(TAG, "Error playing sound", e);
            }


            // Update user score in database
            int userId = sessionManager.getUserId();
            
            executor.execute(() -> {
                try {
                    // Add XP based on lesson difficulty
                    int xpPoints = 5;
                    quizService.addXpPoints(userId, currentLessonId, xpPoints);
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        firebaseDataManager.syncUserProgress(userId, currentLessonId);
                    });
//                    quizService.updateUserStreak(userId);
                } catch (Exception e) {
                    Log.e(TAG, "Error updating score: " + e.getMessage(), e);
                }
            });

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
        btnCheckAnswer.setOnClickListener(v -> checkAnswer());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release MediaPlayer resources
        if (correctSoundPlayer != null) {
            correctSoundPlayer.release();
            correctSoundPlayer = null;
        }
        
        // Shutdown executor
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        
        int userId = sessionManager.getUserId();
        executor.execute(() -> {
            firebaseDataManager.syncUserProgress(userId, 1);
        });
        
        navigationService.navigateToMain();
    }


}
