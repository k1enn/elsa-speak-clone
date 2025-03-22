package com.example.elsa_speak_clone.database.entities;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "UserScores",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "UserId",
                        childColumns = "UserId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Quiz.class,
                        parentColumns = "QuizId",
                        childColumns = "QuizId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {@Index("UserId"), @Index("QuizId")})
public class UserScore {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "ScoreId")
    private int scoreId;

    @ColumnInfo(name = "UserId")
    private int userId;

    @ColumnInfo(name = "QuizId")
    private int quizId;

    @ColumnInfo(name = "Score")
    private int score;

    @ColumnInfo(name = "AttemptDate")
    private String attemptDate;

    @ColumnInfo(name = "LessonId")
    private int lessonId;

    // Constructor
    @Ignore
    public UserScore(int scoreId, int userId, int quizId, int score, String attemptDate) {
        this.scoreId = scoreId;
        this.userId = userId;
        this.quizId = quizId;
        this.score = score;
        this.attemptDate = attemptDate;
    }

    public UserScore() {
        
    }

    // Getters and setters
    public int getScoreId() {
        return scoreId;
    }

    public void setScoreId(int scoreId) {
        this.scoreId = scoreId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getAttemptDate() {
        return attemptDate;
    }

    public void setAttemptDate(String attemptDate) {
        this.attemptDate = attemptDate;
    }

    public int getLessonId() {
        return lessonId;
    }

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
    }


    public void setDateUpdated(String currentDate) {
        // This method should update the attemptDate field
        this.attemptDate = currentDate;
    }

   
}