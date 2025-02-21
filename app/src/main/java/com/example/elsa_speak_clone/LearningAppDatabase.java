package com.example.elsa_speak_clone;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class LearningAppDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "elsa_speak_clone.db";
    private static final int DATABASE_VERSION = 1;

    // Users Table
    private static final String TABLE_USERS = "Users";
    private static final String COLUMN_USER_ID = "UserId";
    private static final String COLUMN_GMAIL = "Gmail";
    private static final String COLUMN_NAME = "Name";
    private static final String COLUMN_JOIN_DATE = "JoinDate";

    // Lessons Table
    private static final String TABLE_LESSONS = "Lessons";
    private static final String COLUMN_LESSON_ID = "LessonId";
    private static final String COLUMN_TOPIC = "Topic";
    private static final String COLUMN_DIFFICULTY_LEVEL = "DifficultyLevel";

    // Vocabulary Table
    private static final String TABLE_VOCABULARY = "Vocabulary";
    private static final String COLUMN_WORD = "Word";
    private static final String COLUMN_PRONUNCIATION = "Pronunciation";
    private static final String COLUMN_WORD_ID = "WordId";

    // UserProgress Table
    private static final String TABLE_USER_PROGRESS = "UserProgress";
    private static final String COLUMN_PROGRESS_ID = "ProgressId";
    private static final String COLUMN_COMPLETION_TIME = "CompletionTime";
    private static final String COLUMN_STREAK = "Streak";
    private static final String COLUMN_LAST_STUDY_DATE = "LastStudyDate";

    // Quizzes Table
    private static final String TABLE_QUIZZES = "Quizzes";
    private static final String COLUMN_QUIZ_ID = "QuizId";
    private static final String COLUMN_QUESTION = "Question";
    private static final String COLUMN_ANSWER = "Answer";

    // UserScores Table
    private static final String TABLE_USER_SCORES = "UserScores";
    private static final String COLUMN_SCORE_ID = "ScoreId";
    private static final String COLUMN_SCORE = "Score";
    private static final String COLUMN_ATTEMPT_DATE = "AttemptDate";

    // SharedResult Table
    private static final String TABLE_SHARED_RESULT = "SharedResult";
    private static final String COLUMN_SHARE_ID = "ShareId";
    private static final String COLUMN_MESSAGE = "Message";
    private static final String COLUMN_SHARE_DATE = "ShareDate";

    public LearningAppDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users Table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_GMAIL + " TEXT NOT NULL, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_JOIN_DATE + " DATE NOT NULL, " +
                "PRIMARY KEY (" + COLUMN_USER_ID + "))";
        db.execSQL(CREATE_USERS_TABLE);

        // Create Lessons Table
        String CREATE_LESSONS_TABLE = "CREATE TABLE " + TABLE_LESSONS + " (" +
                COLUMN_LESSON_ID + " INTEGER NOT NULL, " +
                COLUMN_TOPIC + " TEXT NOT NULL, " +
                COLUMN_DIFFICULTY_LEVEL + " INTEGER NOT NULL, " +
                "PRIMARY KEY (" + COLUMN_LESSON_ID + "))";
        db.execSQL(CREATE_LESSONS_TABLE);

        // Create Vocabulary Table
        String CREATE_VOCABULARY_TABLE = "CREATE TABLE " + TABLE_VOCABULARY + " (" +
                COLUMN_WORD + " TEXT NOT NULL, " +
                COLUMN_PRONUNCIATION + " TEXT NOT NULL, " +
                COLUMN_WORD_ID + " INTEGER NOT NULL, " +
                COLUMN_LESSON_ID + " INTEGER NOT NULL, " +
                "PRIMARY KEY (" + COLUMN_WORD_ID + "), " +
                "FOREIGN KEY (" + COLUMN_LESSON_ID + ") REFERENCES " + TABLE_LESSONS + "(" + COLUMN_LESSON_ID + "))";
        db.execSQL(CREATE_VOCABULARY_TABLE);

        // Create UserProgress Table
        String CREATE_USER_PROGRESS_TABLE = "CREATE TABLE " + TABLE_USER_PROGRESS + " (" +
                COLUMN_PROGRESS_ID + " INTEGER NOT NULL, " +
                COLUMN_DIFFICULTY_LEVEL + " INTEGER NOT NULL, " +
                COLUMN_COMPLETION_TIME + " DATE NOT NULL, " +
                COLUMN_STREAK + " INTEGER NOT NULL, " +
                COLUMN_LAST_STUDY_DATE + " DATE NOT NULL, " +
                COLUMN_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_LESSON_ID + " INTEGER NOT NULL, " +
                "PRIMARY KEY (" + COLUMN_PROGRESS_ID + "), " +
                "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "), " +
                "FOREIGN KEY (" + COLUMN_LESSON_ID + ") REFERENCES " + TABLE_LESSONS + "(" + COLUMN_LESSON_ID + "))";
        db.execSQL(CREATE_USER_PROGRESS_TABLE);

        // Create Quizzes Table
        String CREATE_QUIZZES_TABLE = "CREATE TABLE " + TABLE_QUIZZES + " (" +
                COLUMN_QUIZ_ID + " INTEGER NOT NULL, " +
                COLUMN_QUESTION + " TEXT NOT NULL, " +
                COLUMN_ANSWER + " TEXT NOT NULL, " +
                COLUMN_LESSON_ID + " INTEGER NOT NULL, " +
                "PRIMARY KEY (" + COLUMN_QUIZ_ID + "), " +
                "FOREIGN KEY (" + COLUMN_LESSON_ID + ") REFERENCES " + TABLE_LESSONS + "(" + COLUMN_LESSON_ID + "))";
        db.execSQL(CREATE_QUIZZES_TABLE);

        // Create UserScores Table
        String CREATE_USER_SCORES_TABLE = "CREATE TABLE " + TABLE_USER_SCORES + " (" +
                COLUMN_SCORE_ID + " INTEGER NOT NULL, " +
                COLUMN_SCORE + " INTEGER NOT NULL, " +
                COLUMN_ATTEMPT_DATE + " DATE NOT NULL, " +
                COLUMN_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_QUIZ_ID + " INTEGER NOT NULL, " +
                "PRIMARY KEY (" + COLUMN_SCORE_ID + "), " +
                "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "), " +
                "FOREIGN KEY (" + COLUMN_QUIZ_ID + ") REFERENCES " + TABLE_QUIZZES + "(" + COLUMN_QUIZ_ID + "))";
        db.execSQL(CREATE_USER_SCORES_TABLE);

        // Create SharedResult Table
        String CREATE_SHARED_RESULT_TABLE = "CREATE TABLE " + TABLE_SHARED_RESULT + " (" +
                COLUMN_SHARE_ID + " INTEGER NOT NULL, " +
                COLUMN_MESSAGE + " TEXT NOT NULL, " +
                COLUMN_SHARE_DATE + " DATE NOT NULL, " +
                COLUMN_USER_ID + " INTEGER NOT NULL, " +
                "PRIMARY KEY (" + COLUMN_SHARE_ID + "), " +
                "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "))";
        db.execSQL(CREATE_SHARED_RESULT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHARED_RESULT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_SCORES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUIZZES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_PROGRESS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VOCABULARY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LESSONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Create tables again
        onCreate(db);
    }

    // Register a User
    public boolean registerUser(String gmail, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Generate unique ID
        int userId = generateUniqueId(db);

        // Add values to columns
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_GMAIL, gmail);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_JOIN_DATE, getCurrentDate());

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    private int generateUniqueId(SQLiteDatabase db) {
        Random random = new Random();
        int userId;
        do {
            userId = 10000 + random.nextInt(90000); // Generates a 5-digit random number
        } while (doesUserIdExist(db, userId));
        return userId;
    }

    private boolean doesUserIdExist(SQLiteDatabase db, int userId) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // For local SQLite authentication
    public boolean authenticateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
            TABLE_USERS,
            new String[]{COLUMN_GMAIL, COLUMN_NAME},
            COLUMN_NAME + "=?",  // Only check username for local auth
            new String[]{username},
            null,
            null,
            null
        );
        
        boolean isAuthenticated = false;
        if (cursor != null) {
            isAuthenticated = cursor.getCount() > 0;
            cursor.close();
        }
        return isAuthenticated;
    }

    // For Firebase authentication
    public boolean doesUserGmailExist(String gmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
            TABLE_USERS,
            new String[]{COLUMN_GMAIL},
            COLUMN_GMAIL + "=?",  // Only check Gmail for Firebase auth
            new String[]{gmail},
            null,
            null,
            null
        );
        
        boolean exists = false;
        if (cursor != null) {
            exists = cursor.getCount() > 0;
            cursor.close();
        }
        return exists;
    }

    // Add Lesson Progress
    public boolean addUserProgress(int userId, int lessonId, int difficultyLevel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Generate unique progress ID
        int progressId = generateUniqueProgressId(db);

        values.put(COLUMN_PROGRESS_ID, progressId);
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_LESSON_ID, lessonId);
        values.put(COLUMN_DIFFICULTY_LEVEL, difficultyLevel);
        values.put(COLUMN_COMPLETION_TIME, getCurrentDate());
        values.put(COLUMN_STREAK, 1);
        values.put(COLUMN_LAST_STUDY_DATE, getCurrentDate());

        long result = db.insert(TABLE_USER_PROGRESS, null, values);
        return result != -1;
    }

    private int generateUniqueProgressId(SQLiteDatabase db) {
        Random random = new Random();
        int progressId;
        do {
            progressId = random.nextInt(1000000);
        } while (doesProgressIdExist(db, progressId));
        return progressId;
    }

    private boolean doesProgressIdExist(SQLiteDatabase db, int progressId) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER_PROGRESS + 
                " WHERE " + COLUMN_PROGRESS_ID + "=?",
                new String[]{String.valueOf(progressId)});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Add Quiz Score
    public boolean addQuizScore(int userId, int quizId, int score) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Generate unique score ID
        int scoreId = generateUniqueScoreId(db);

        values.put(COLUMN_SCORE_ID, scoreId);
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_QUIZ_ID, quizId);
        values.put(COLUMN_SCORE, score);
        values.put(COLUMN_ATTEMPT_DATE, getCurrentDate());

        long result = db.insert(TABLE_USER_SCORES, null, values);
        return result != -1;
    }

    private int generateUniqueScoreId(SQLiteDatabase db) {
        Random random = new Random();
        int scoreId;
        do {
            scoreId = random.nextInt(1000000);
        } while (doesScoreIdExist(db, scoreId));
        return scoreId;
    }

    private boolean doesScoreIdExist(SQLiteDatabase db, int scoreId) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER_SCORES + 
                " WHERE " + COLUMN_SCORE_ID + "=?",
                new String[]{String.valueOf(scoreId)});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Get User Progress
    public Cursor getUserProgress(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
            "SELECT l.Topic, up.DifficultyLevel, up.CompletionTime, up.Streak " +
            "FROM " + TABLE_USER_PROGRESS + " up " +
            "JOIN " + TABLE_LESSONS + " l ON up.LessonId = l.LessonId " +
            "WHERE up.UserId = ?",
            new String[]{String.valueOf(userId)}
        );
    }

    // Get User's Quiz Scores
    public Cursor getUserScores(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
            "SELECT q.Question, us.Score, us.AttemptDate " +
            "FROM " + TABLE_USER_SCORES + " us " +
            "JOIN " + TABLE_QUIZZES + " q ON us.QuizId = q.QuizId " +
            "WHERE us.UserId = ? " +
            "ORDER BY us.AttemptDate DESC",
            new String[]{String.valueOf(userId)}
        );
    }

    public int getUserId(String emailOrUsername) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID},
                COLUMN_GMAIL + "=? OR " + COLUMN_NAME + "=?", 
                new String[]{emailOrUsername, emailOrUsername}, 
                null, null, null);
        
        int userId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
            cursor.close();
        }
        return userId;
    }

    public boolean doesUserExist(String gmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
            TABLE_USERS,
            new String[]{COLUMN_GMAIL},
            COLUMN_GMAIL + "=?",
            new String[]{gmail},
            null,
            null,
            null
        );
        
        boolean exists = false;
        if (cursor != null) {
            exists = cursor.getCount() > 0;
            cursor.close();
        }
        return exists;
    }

    private boolean doesUserIdExist(SQLiteDatabase db, int userId) {
        Cursor cursor = db.query(
            TABLE_USERS,
            new String[]{COLUMN_USER_ID},
            COLUMN_USER_ID + "=?",
            new String[]{String.valueOf(userId)},
            null,
            null,
            null
        );
        
        boolean exists = false;
        if (cursor != null) {
            exists = cursor.getCount() > 0;
            cursor.close();
        }
        return exists;
    }
}
