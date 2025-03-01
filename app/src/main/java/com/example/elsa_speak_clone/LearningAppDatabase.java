package com.example.elsa_speak_clone;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import android.util.Log;

public class LearningAppDatabase extends SQLiteOpenHelper {
    private Context context;
    private static final String TAG = "LearningAppDatabase";
    private static final String DATABASE_NAME = "elsa_speak_clone.db";
    private static final int DATABASE_VERSION = 1;
    private static final String emptyString = "";

    // Users Table
    private static final String TABLE_USERS = "Users";
    private static final String COLUMN_USER_ID = "UserId";
    private static final String COLUMN_GMAIL = "Gmail";
    private static final String COLUMN_NAME = "Name";
    private static final String COLUMN_JOIN_DATE = "JoinDate";
    private static final String COLUMN_PASSWORD = "Password";

    // Lessons Table
    private static final String TABLE_LESSONS = "Lessons";
    private static final String COLUMN_LESSON_ID = "LessonId";
    private static final String COLUMN_LESSON_CONTENT = "LessonContent";
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
    private static final String COLUMN_XP = "Xp";
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
        this.context = context;
        Log.d(TAG, "LearningAppDatabase");
    }
    // Create Users Table with password
    private String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
            COLUMN_USER_ID + " INTEGER NOT NULL, " +
            COLUMN_GMAIL + " TEXT, " +
            COLUMN_NAME + " TEXT NOT NULL, " +
            COLUMN_PASSWORD + " TEXT, " +
            COLUMN_JOIN_DATE + " DATE NOT NULL, " +
            "PRIMARY KEY (" + COLUMN_USER_ID + "))";

    // Create Lessons Table
    private String CREATE_LESSONS_TABLE = "CREATE TABLE " + TABLE_LESSONS + " (" +
            COLUMN_LESSON_ID + " INTEGER NOT NULL, " +
            COLUMN_TOPIC + " TEXT NOT NULL, " +
            COLUMN_LESSON_CONTENT + " TEXT NOT NULL, " +
            COLUMN_DIFFICULTY_LEVEL + " INTEGER NOT NULL, " +
            "PRIMARY KEY (" + COLUMN_LESSON_ID + "))";

    // Create Vocabulary Table
    private String CREATE_VOCABULARY_TABLE = "CREATE TABLE " + TABLE_VOCABULARY + " (" +
            COLUMN_WORD + " TEXT NOT NULL, " +
            COLUMN_PRONUNCIATION + " TEXT NOT NULL, " +
            COLUMN_WORD_ID + " INTEGER NOT NULL, " +
            COLUMN_LESSON_ID + " INTEGER NOT NULL, " +
            "PRIMARY KEY (" + COLUMN_WORD_ID + "), " +
            "FOREIGN KEY (" + COLUMN_LESSON_ID + ") REFERENCES " + TABLE_LESSONS + "(" + COLUMN_LESSON_ID + "))";

    // Create UserProgress Table
    private String CREATE_USER_PROGRESS_TABLE = "CREATE TABLE " + TABLE_USER_PROGRESS + " (" +
            COLUMN_PROGRESS_ID + " INTEGER NOT NULL, " +
            COLUMN_DIFFICULTY_LEVEL + " INTEGER NOT NULL, " +
            COLUMN_COMPLETION_TIME + " DATE NOT NULL, " +
            COLUMN_STREAK + " INTEGER NOT NULL, " +
            COLUMN_XP + " INTEGER, " +
            COLUMN_LAST_STUDY_DATE + " DATE NOT NULL, " +
            COLUMN_USER_ID + " INTEGER NOT NULL, " +
            COLUMN_LESSON_ID + " INTEGER NOT NULL, " +
            "PRIMARY KEY (" + COLUMN_PROGRESS_ID + "), " +
            "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "), " +
            "FOREIGN KEY (" + COLUMN_LESSON_ID + ") REFERENCES " + TABLE_LESSONS + "(" + COLUMN_LESSON_ID + "))";

    // Create Quizzes Table
    private String CREATE_QUIZZES_TABLE = "CREATE TABLE " + TABLE_QUIZZES + " (" +
            COLUMN_QUIZ_ID + " INTEGER NOT NULL, " +
            COLUMN_QUESTION + " TEXT NOT NULL, " +
            COLUMN_ANSWER + " TEXT NOT NULL, " +
            COLUMN_LESSON_ID + " INTEGER NOT NULL, " +
            "PRIMARY KEY (" + COLUMN_QUIZ_ID + "), " +
            "FOREIGN KEY (" + COLUMN_LESSON_ID + ") REFERENCES " + TABLE_LESSONS + "(" + COLUMN_LESSON_ID + "))";

    // Create UserScores Table
    private String CREATE_USER_SCORES_TABLE = "CREATE TABLE " + TABLE_USER_SCORES + " (" +
            COLUMN_SCORE_ID + " INTEGER NOT NULL, " +
            COLUMN_SCORE + " INTEGER NOT NULL, " +
            COLUMN_ATTEMPT_DATE + " DATE NOT NULL, " +
            COLUMN_USER_ID + " INTEGER NOT NULL, " +
            COLUMN_QUIZ_ID + " INTEGER NOT NULL, " +
            "PRIMARY KEY (" + COLUMN_SCORE_ID + "), " +
            "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "), " +
            "FOREIGN KEY (" + COLUMN_QUIZ_ID + ") REFERENCES " + TABLE_QUIZZES + "(" + COLUMN_QUIZ_ID + "))";

    // Create SharedResult Table
    private String CREATE_SHARED_RESULT_TABLE = "CREATE TABLE " + TABLE_SHARED_RESULT + " (" +
            COLUMN_SHARE_ID + " INTEGER NOT NULL, " +
            COLUMN_MESSAGE + " TEXT NOT NULL, " +
            COLUMN_SHARE_DATE + " DATE NOT NULL, " +
            COLUMN_USER_ID + " INTEGER NOT NULL, " +
            "PRIMARY KEY (" + COLUMN_SHARE_ID + "), " +
            "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "))";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        Log.d(TAG, "Create USERS");

        // Create LESSONS table
        db.execSQL(CREATE_LESSONS_TABLE);
        Log.d(TAG, "Create LESSONS");
        // Insert default LESSONS data
        insertDefaultLessons(db);
        Log.d(TAG, "Insert LESSONS");

        // Create VOCABULARY table
        db.execSQL(CREATE_VOCABULARY_TABLE);
        Log.d(TAG, "Create VOCABULARY");
        // Insert default VOCABULARY data
        insertDefaultVocabulary(db);
        Log.d(TAG, "Insert VOCABULARY");

        db.execSQL(CREATE_USER_PROGRESS_TABLE);
        Log.d(TAG, "Create USER PROGRESS");

        db.execSQL(CREATE_QUIZZES_TABLE);
        Log.d(TAG, "Create QUIZZES");

        db.execSQL(CREATE_USER_SCORES_TABLE);
        Log.d(TAG, "Create USER SCORES");

        db.execSQL(CREATE_SHARED_RESULT_TABLE);
        Log.d(TAG, "Create SHARED RESULT");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.d(TAG, "onUpgrade");

      if(oldVersion < 2)
      dropAllTables(db);
    }

    // For local SQLite authentication
    // Check if user exist
    public boolean authenticateUser(String username, String password) {
        if (username == null || password == null) return false;
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
            TABLE_USERS,
            new String[]{COLUMN_NAME, COLUMN_PASSWORD},  // Get both username and password
            COLUMN_NAME + "=?",
            new String[]{username},
            null,
            null,
            null
        );
        
        boolean isAuthenticated = false;
        if (cursor != null && cursor.moveToFirst()) {
            String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
            // Check if both username and password match
            isAuthenticated = storedPassword != null && storedPassword.equals(password);
            cursor.close();
        }
        return isAuthenticated;
    }

    // For Firebase authentication
    public boolean doesUserGmailExist(String gmail) {
        if (gmail == null) return false;
        
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

    // Register a new user (works for both local and Firebase)
    public boolean registerUser(String name, String password) {
        if (name == null || password == null) return false;
        
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        int userId = generateUniqueId(db);
        values.put(COLUMN_USER_ID, userId);
        
        if (name.contains("@")) {
            String username = name.substring(0, name.indexOf("@"));
            // Google account registration
            values.put(COLUMN_GMAIL, name); // Put user's mail in GMAIL
            values.put(COLUMN_NAME, username); // Put username by only take characters before "@"
            values.put(COLUMN_PASSWORD, emptyString);  // No password for Google accounts
        } else {
            // Local account registration
            values.put(COLUMN_NAME, name);
            values.put(COLUMN_GMAIL, emptyString);
            values.put(COLUMN_PASSWORD, password);  // Store password for local accounts
        }
        
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
        Log.d("Generate_ID", "Current ID:" +userId);
        return userId;
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
            "SELECT l.Topic up.DifficultyLevel, up.CompletionTime, up.Streak " +
            "FROM " + TABLE_USER_PROGRESS + " up " +
            "JOIN " + TABLE_LESSONS + " l ON up.LessonId = l.LessonId " +
            "WHERE up.UserId = ?",
            new String[]{String.valueOf(userId)}
        );
    }
    public void injectUserStreak(int userId, int streak) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Prepare values for updating/inserting
        ContentValues values = new ContentValues();
        values.put(COLUMN_STREAK, streak);
        values.put(COLUMN_LAST_STUDY_DATE, getCurrentDate()); // Update last study date

        // Check if user progress already exists
        Cursor cursor = getUserProgress(userId);

        if (cursor != null && cursor.moveToFirst()) {
            // Update existing user streak
            db.update(TABLE_USER_PROGRESS, values, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)});
            Log.d("Injection", "Updated new streak" + userId);
        } else {
            // Insert new record if not exists
            values.put(COLUMN_USER_ID, userId);
            db.insert(TABLE_USER_PROGRESS, null, values);
            Log.d("Injection", "Inserted new streak" + userId);
        }

        if (cursor != null) {
            cursor.close();
        }
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

    public boolean isUsernameAvailable(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
            TABLE_USERS,
            new String[]{COLUMN_NAME},
            COLUMN_NAME + "=? OR " + COLUMN_GMAIL + "=?",  // Check both name and gmail
            new String[]{username, username},
            null,
            null,
            null
        );
        
        boolean isAvailable = true;  // Username is available by default
        if (cursor != null) {
            isAvailable = cursor.getCount() == 0;  // Available if no matches found
            cursor.close();
        }
        return isAvailable;
    }

    // Delele user function just in case
    public int deleteUser(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_USERS, COLUMN_USER_ID+"=?", new String[] {String.valueOf(id)});
    }

    private void dropAllTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHARED_RESULT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_SCORES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUIZZES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_PROGRESS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VOCABULARY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LESSONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        onCreate(db);
    }

    private void insertDefaultLessons(SQLiteDatabase db) {
        // Define an array of lessons to insert with content
        String[][] lessonsData = {
                {"1", "Basic Greetings", "In this lesson, you will learn common English greetings such as 'Hello', 'Good morning', and 'How are you?'. Practice these phrases daily to improve your conversational skills.", "1"},
                {"2", "Daily Conversations", "This lesson covers everyday conversations including asking for directions, ordering food, and making small talk. These phrases will help you navigate common social situations.", "1"},
                {"3", "Travel Phrases", "Learn essential phrases for traveling abroad including how to ask for help, book accommodations, and navigate public transportation. This vocabulary is crucial for international travelers.", "2"},
                {"4", "Business English", "This lesson focuses on professional English used in workplace settings, including email writing, meeting vocabulary, and negotiation phrases. Mastering these terms will enhance your career prospects.", "2"},
                {"5", "Academic Vocabulary", "Explore advanced vocabulary commonly used in academic settings, research papers, and scholarly discussions. This lesson will help improve your formal writing and comprehension of academic texts.", "3"}
        };

        // Insert each lesson
        for (String[] lesson : lessonsData) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_LESSON_ID, Integer.parseInt(lesson[0]));
            values.put(COLUMN_TOPIC, lesson[1]);
            values.put(COLUMN_LESSON_CONTENT, lesson[2]);
            values.put(COLUMN_DIFFICULTY_LEVEL, Integer.parseInt(lesson[3]));
            db.insert(TABLE_LESSONS, null, values);
        }
    }

    private void insertDefaultVocabulary(SQLiteDatabase db) {
        // Define vocabulary data: word, pronunciation, wordId, lessonId
        String[][] vocabularyData = {
                {"Hello", "həˈloʊ", "1", "1"},
                {"Goodbye", "ɡʊdˈbaɪ", "2", "1"},
                {"Thank you", "θæŋk juː", "3", "1"},
                {"Excuse me", "ɪkˈskjuːz miː", "4", "1"},
                {"Good morning", "ɡʊd ˈmɔːrnɪŋ", "5", "1"},
                {"How are you?", "haʊ ɑr juː", "6", "1"},
                {"Airport", "ˈɛrpɔːrt", "7", "3"},
                {"Hotel", "hoʊˈtɛl", "8", "3"},
                {"Taxi", "ˈtæksi", "9", "3"},
                {"Restaurant", "ˈrɛstərɒnt", "10", "3"},
                {"Meeting", "ˈmiːtɪŋ", "11", "4"},
                {"Presentation", "ˌprezənˈteɪʃən", "12", "4"},
                {"Conference", "ˈkɒnfərəns", "13", "4"},
                {"Research", "rɪˈsɜːrtʃ", "14", "5"},
                {"Analysis", "əˈnæləsɪs", "15", "5"}
        };

        // Insert each vocabulary item using ContentValues
        for (String[] vocab : vocabularyData) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_WORD, vocab[0]);
            values.put(COLUMN_PRONUNCIATION, vocab[1]);
            values.put(COLUMN_WORD_ID, Integer.parseInt(vocab[2]));
            values.put(COLUMN_LESSON_ID, Integer.parseInt(vocab[3]));
            db.insert(TABLE_VOCABULARY, null, values);
        }
    }

    // Update User's streak everytime login/.
    public void updateUserStreak(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = getUserProgress(userId);

        if (cursor != null && cursor.moveToFirst()) {
            try {
                // Get last study date and current streak
                String lastStudyDateStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_STUDY_DATE));
                int currentStreak = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STREAK));

                // Get today's date
                String todayStr = getCurrentDate();

                // Convert last study date to Date object
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date lastStudyDate = sdf.parse(lastStudyDateStr);
                Date today = sdf.parse(todayStr);

                // Calculate difference in days
                long diff;
                diff = TimeUnit.DAYS.convert(today.getTime() - lastStudyDate.getTime(), TimeUnit.MILLISECONDS);

                if (diff == 1) {
                    // Studied yesterday → Increment streak
                    currentStreak++;
                } else if (diff > 1) {
                    // Missed a day → Reset streak
                    currentStreak = 1;
                }

                // Update the database
                ContentValues values = new ContentValues();
                values.put(COLUMN_STREAK, currentStreak);
                values.put(COLUMN_LAST_STUDY_DATE, todayStr); // Update last study date to today

                db.update(TABLE_USER_PROGRESS, values, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
    }


}
