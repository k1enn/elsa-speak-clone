package com.example.elsa_speak_clone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LearningAppDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "elsa_speak_clone.db";
    private static final int DATABASE_VERSION = 1;

    // Users Table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_DAY_STREAK = "day_streak";
    private static final String COLUMN_XP_POINTS = "xp_points";
    private static final String COLUMN_LEVEL = "level";

    // Lessons Table
    private static final String TABLE_LESSONS = "lessons";
    private static final String COLUMN_LESSON_ID = "lesson_id";
    private static final String COLUMN_LESSON_NAME = "lesson_name";
    private static final String COLUMN_LESSON_STATUS = "status"; // Completed or Not

    // Pronunciation Scores Table
    private static final String TABLE_PRONUNCIATION = "pronunciation_scores";
    private static final String COLUMN_WORD = "word_pronounced";
    private static final String COLUMN_SCORE = "score";
    private static final String COLUMN_DATE_TIME = "date_time";

    public LearningAppDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users Table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_DAY_STREAK + " INTEGER DEFAULT 1, " +
                COLUMN_XP_POINTS + " INTEGER DEFAULT 0, " +
                COLUMN_LEVEL + " INTEGER DEFAULT 1)";
        db.execSQL(CREATE_USERS_TABLE);


        // Create Lessons Table
        String CREATE_LESSONS_TABLE = "CREATE TABLE " + TABLE_LESSONS + " (" +
                COLUMN_LESSON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_LESSON_NAME + " TEXT, " +
                COLUMN_LESSON_STATUS + " TEXT, " +
                "FOREIGN KEY (" + COLUMN_USERNAME + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USERNAME + "))";
        db.execSQL(CREATE_LESSONS_TABLE);

        // Create Pronunciation Scores Table
        String CREATE_PRONUNCIATION_TABLE = "CREATE TABLE " + TABLE_PRONUNCIATION + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_WORD + " TEXT, " +
                COLUMN_SCORE + " INTEGER, " +
                COLUMN_DATE_TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (" + COLUMN_USERNAME + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USERNAME + "))";
        db.execSQL(CREATE_PRONUNCIATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LESSONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRONUNCIATION);
        onCreate(db);
    }

    // Register a User
    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // Authenticate User (Login)
    public boolean authenticateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS +
                        " WHERE " + COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password});

        boolean success = cursor.getCount() > 0;
        cursor.close();
        return success;
    }

    // Check Username Availability
    public boolean isUsernameAvailable(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USERNAME},
                COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);

        boolean available = cursor.getCount() == 0;
        cursor.close();
        return available;
    }

    // Update User XP and Streak
    public void updateUserProgress(String username, int xpGained) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_XP_POINTS, COLUMN_DAY_STREAK},
                COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);

        if (cursor.moveToFirst()) {
            int currentXP = cursor.getInt(0);
            int currentStreak = cursor.getInt(1);

            ContentValues values = new ContentValues();
            values.put(COLUMN_XP_POINTS, currentXP + xpGained);
            values.put(COLUMN_DAY_STREAK, currentStreak + 1); // Increase streak

            db.update(TABLE_USERS, values, COLUMN_USERNAME + "=?", new String[]{username});
        }
        cursor.close();
    }

    // Add Lesson Progress
    public boolean addLessonProgress(String username, String lessonName, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_LESSON_NAME, lessonName);
        values.put(COLUMN_LESSON_STATUS, status);

        long result = db.insert(TABLE_LESSONS, null, values);
        return result != -1;
    }

    // Store Pronunciation Score
    public boolean addPronunciationScore(String username, String word, int score) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_WORD, word);
        values.put(COLUMN_SCORE, score);

        long result = db.insert(TABLE_PRONUNCIATION, null, values);
        return result != -1;
    }

    // Retrieve User Progress
    public Cursor getUserProgress(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + "=?", new String[]{username});
    }

    // Get User's Completed Lessons
    public Cursor getCompletedLessons(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_LESSONS + " WHERE " + COLUMN_USERNAME + "=? AND " +
                COLUMN_LESSON_STATUS + "='Completed'", new String[]{username});
    }

    // Use for testing
    public void injectProgress(String username, int xpPoints, int dayStreak) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Prepare the ContentValues to update the user progress
        ContentValues values = new ContentValues();
        values.put(COLUMN_XP_POINTS, xpPoints);
        values.put(COLUMN_DAY_STREAK, dayStreak);

        // Update the user's progress based on the username
        int rowsUpdated = db.update(TABLE_USERS, values, COLUMN_USERNAME + " = ?", new String[]{username});

        // If rowsUpdated is greater than 0, it means the user was found and updated
        if (rowsUpdated > 0) {
            Log.d("Database", "User progress updated successfully.");
        } else {
            Log.d("Database", "Failed to update user progress.");
        }
    }

}
