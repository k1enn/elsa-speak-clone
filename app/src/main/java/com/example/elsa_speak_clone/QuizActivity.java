package com.example.elsa_speak_clone;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class QuizActivity extends AppCompatActivity {
    private LearningAppDatabase dbHelper;
    private TextView tvQuestion, tvResult;
    private EditText etAnswer;
    private Button btnCheckAnswer, btnNextQuestion;
    private String correctAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        dbHelper = new LearningAppDatabase(this);
        tvQuestion = findViewById(R.id.tvQuestion);
        etAnswer = findViewById(R.id.etAnswer);
        btnCheckAnswer = findViewById(R.id.btnCheckAnswer);
        btnNextQuestion = findViewById(R.id.btnNextQuestion);
        tvResult = findViewById(R.id.tvResult);

        loadNextQuestion();

        btnCheckAnswer.setOnClickListener(v -> checkAnswer());
        btnNextQuestion.setOnClickListener(v -> loadNextQuestion());
    }

    private void loadNextQuestion() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT QuizId, Question, Answer FROM Quizzes ORDER BY RANDOM() LIMIT 1", null);

        if (!cursor.moveToFirst()) {
         //   String question = cursor.getString(1);
            String question = "How ___ you?";
         //   correctAnswer = cursor.getString(2).trim(); // Lấy đáp án đúng từ DB
            correctAnswer = "are";
            tvQuestion.setText(question);
            etAnswer.setText(""); // Xóa input cũ
            tvResult.setText("");
            btnNextQuestion.setVisibility(Button.GONE);// Ẩn kết quả cũ
        }
        else {
            tvQuestion.setText("Chưa có câu hỏi nào trong database!");
            correctAnswer = "";
        }
        cursor.close();
        db.close();
    }

    private void checkAnswer() {
        String userAnswer = etAnswer.getText().toString().trim().replaceAll("\\s+", " ");

        if (userAnswer.equalsIgnoreCase(correctAnswer.replaceAll("\\s+", " ")))
        {
            tvResult.setText("✅ Đúng!");
            tvResult.setTextColor(Color.GREEN);
        }
        else
        {
            tvResult.setText("❌ Sai! Đáp án đúng là: " + correctAnswer);
            tvResult.setTextColor(Color.RED);
        }
        btnNextQuestion.setVisibility(Button.VISIBLE);
    }
}
