package com.example.elsa_speak_clone.activities;

import android.animation.Animator;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.media.MediaPlayer;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.airbnb.lottie.LottieAnimationView;
import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.SessionManager;
import com.example.elsa_speak_clone.database.entities.Lesson;
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
    private TextView tvTitle;
    private TextView tvQuestion, tvResult;
    private EditText etAnswer;
    private Button btnCheckAnswer, btnNextQuestion;
    private Toolbar toolbar;
    private LottieAnimationView lottieConfetti;

    private int totalAvailableQuestions = 0;
    // Quiz data
    private String correctAnswer;
    private int currentLessonId = 1; // Default to lesson 1
    private List<Integer> usedQuizIds = new ArrayList<>(); // Track used questions
    private MediaPlayer correctSoundPlayer; // For the "ting ting" sound
    private PopupWindow confettiPopup;

    // Thread management
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private int correctAnswersCount = 0;
    private int totalQuestionsAttempted = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        initializeServices();
        initializeUI();
        setupToolbar ();
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
        // Get the lesson ID
        if (getIntent().hasExtra("LESSON_ID")) {
            currentLessonId = getIntent().getIntExtra("LESSON_ID", 1);
        }
        tvTitle = findViewById (R.id.tvTitle);
        toolbar = findViewById (R.id.toolbar) ;
        tvQuestion = findViewById(R.id.tvQuestion);
        etAnswer = findViewById(R.id.etAnswer);
        btnCheckAnswer = findViewById(R.id.btnCheckAnswer);
        btnNextQuestion = findViewById(R.id.btnNextQuestion);
        tvResult = findViewById(R.id.tvResult);
        
        // Initialize the confetti
        lottieConfetti = findViewById(R.id.lottieConfetti);
        
        // Initialize the sound player
        correctSoundPlayer = MediaPlayer.create(this, R.raw.correct_sound);
        
        // Setup check answer button default state
        returnCheckAnswerButton();
    }
    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d("PronunciationActivity", "onOptionsItemSelected: " + item.getItemId());

        // Handle the back button (up button in action bar)
        if (item.getItemId() == android.R.id.home) {
            Log.d("PronunciationActivity", "Back button pressed");
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        Log.d("Quiz", "onBackPressed called");
        Log.d("Quiz", "isTaskRoot: " + isTaskRoot());
        super.onBackPressed();
    }
    private void loadNextQuestion() {
        // Reset UI for new question
        etAnswer.setText("");
        tvResult.setText("");
        returnCheckAnswerButton();

        executor.execute(() -> {
            try {
                // Count total questions if not already done
                if (totalAvailableQuestions == 0) {
                    totalAvailableQuestions = database.quizDao().getQuizCountForLesson (currentLessonId);
                }
                
                // Get a random quiz that hasn't been used yet in this session
                Quiz quiz = database.quizDao().getRandomQuizForLessonExcludingIds(
                        currentLessonId, usedQuizIds.toArray(new Integer[0]));
                Lesson lesson = database.lessonDao ().getLessonById (currentLessonId);
                if (quiz != null) {
                    // Add this quiz ID to our used list
                    usedQuizIds.add(quiz.getQuizId());

                    // Update UI on main thread
                    final String question = quiz.getQuestion();
                    final String answer = quiz.getAnswer();

                    runOnUiThread(() -> {
                        tvTitle.setText (lesson.getTopic ());
                        tvQuestion.setText(question);
                        correctAnswer = answer;
                    });
                } else {
                    // No more question, end.
                    showCongratulationsPopup ();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading next question: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(QuizActivity.this, 
                            "Error loading question: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
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
        totalQuestionsAttempted++;

        // If user correct
        if (userAnswer.equalsIgnoreCase(correctAnswer.replaceAll("\\s+", " "))) {
            tvResult.setText("✅ Correct!");
            tvResult.setTextColor(Color.GREEN);
            correctAnswersCount++; // Track correct answers

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

                    // Add to local
                    quizService.addXpPoints(userId, currentLessonId, xpPoints);


                    // Sync to Firebase
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        firebaseDataManager.syncUserProgress(userId, currentLessonId);
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error updating score: " + e.getMessage(), e);
                }
            });

            changeCheckAnswerButton();
        } else {
            tvResult.setText("❌ Incorrect! The correct answer is: " + correctAnswer);
            tvResult.setTextColor(Color.RED);
        }
        
        // Check if run out of questions
        if (usedQuizIds.size() >= totalAvailableQuestions) {
            showCongratulationsPopup ();
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
    
}
