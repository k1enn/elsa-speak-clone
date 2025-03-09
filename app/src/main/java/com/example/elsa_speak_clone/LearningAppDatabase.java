package com.example.elsa_speak_clone;


import android.content.SharedPreferences;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

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
import org.mindrot.jbcrypt.BCrypt;

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
    private static final String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
            COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_GMAIL + " TEXT, " +
            COLUMN_NAME + " TEXT NOT NULL, " +
            COLUMN_PASSWORD + " TEXT, " +
            COLUMN_JOIN_DATE + " DATE NOT NULL)";

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
            COLUMN_STREAK + " INTEGER, " +
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
      if (oldVersion < 2) {
         db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN last_login DATETIME");
      }
      dropAllTables(db);
    }

    /**
     * Authenticate a user with username and password
     * @param username The username to check
     * @param password The password to verify
     * @return true if authentication is successful, false otherwise
     * So it basically Login function
     */

    public boolean authenticateUser(String username, String password) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = null;
        SessionManager sessionManager = new SessionManager(this.context);
        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_USER_ID, COLUMN_PASSWORD},
                COLUMN_NAME + "=?",
                new String[]{username},
                null, null, null)) {
            if (cursor.moveToFirst()) {
                String authenticateHashedPassword = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
                try {
                    boolean passwordValidate = BCrypt.checkpw(password, authenticateHashedPassword);
                    if (passwordValidate) {
                        sessionManager.createSession(username, getUserId(username));
                        return true;
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Can not authenticate password", e);
                }

            }
            return false;


        }
         catch (Exception e) {
            Log.e(TAG, "Error authenticating user: " + e.getMessage());
            return false;
        }
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

    private void handleGoogleRegistration(ContentValues values, String email) {
        try {
            String username = email.split("@")[0];
            values.put(COLUMN_GMAIL, email);
            values.put(COLUMN_NAME, username);
            values.put(COLUMN_PASSWORD, ""); // Empty password for social
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    // Register a new user (works for both local and Firebase)
    public boolean registerUser(String name, String password) {
        if (name == null || password == null) return false;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
       SessionManager sessionManager = new SessionManager(this.context);
        int userId = -1;

        try {
            userId = generateUniqueId(db);
            values.put(COLUMN_USER_ID, userId);
        } catch (Exception e) {
            Log.e(TAG, "Can not generate unique user id");
        }
                if(name.contains("@")) {
                    sessionManager.createSession(name, userId);
                    handleGoogleRegistration(values, name);
                }
         else {
try {
    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
    // Local account registration
    values.put(COLUMN_NAME, name);
    values.put(COLUMN_GMAIL, emptyString);
    values.put(COLUMN_PASSWORD, hashedPassword);
    sessionManager.createSession(name, userId);
    Log.d(TAG, "Hased password" + hashedPassword);
}catch (Exception e) {
                Log.d(TAG, "Can not register LOCAL user.");
            }
        }

        values.put(COLUMN_JOIN_DATE, getCurrentDate());

        long result = db.insert(TABLE_USERS, null, values);

if(result != -1) {
        return true;
    } else{
        return false;
    }
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
        SessionManager sessionManager = new SessionManager(this.context);
        userId = Integer.parseInt(sessionManager.getUserDetails().get("userId"));

        return db.rawQuery(
                "SELECT up." + COLUMN_PROGRESS_ID + ", l." + COLUMN_TOPIC +
                        ", up." + COLUMN_DIFFICULTY_LEVEL + ", up." + COLUMN_COMPLETION_TIME +
                        ", up." + COLUMN_STREAK + ", up." + COLUMN_LAST_STUDY_DATE +
                        ", up." + COLUMN_XP +
                        " FROM " + TABLE_USER_PROGRESS + " up " +
                        "JOIN " + TABLE_LESSONS + " l ON up." + COLUMN_LESSON_ID + " = l." + COLUMN_LESSON_ID +
                        " WHERE up." + COLUMN_USER_ID + " = ?",
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
    // Update User's streak everytime login/.
    /**
     * Update user's streak based on login date
     * @param context The application context to access SharedPreferences
     */
    public void updateUserStreak(Context context) {
        SessionManager sessionManager = new SessionManager(this.context);
        int userId = Integer.parseInt(sessionManager.getUserDetails().get("userId"));

        if (userId < 0) {
            Log.e(TAG, "Cannot update streak: Invalid user ID");
            return;
        }

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
                Date lastStudyDate = null;
                try {
                    lastStudyDate = sdf.parse(lastStudyDateStr);
                } catch (Exception e) {
                    Log.e(TAG, "Error data parsing in updateUserStreak");
                    return;
                }
                Date today = sdf.parse(todayStr);

                // Calculate difference in days
                long diff = TimeUnit.DAYS.convert(today.getTime() - lastStudyDate.getTime(), TimeUnit.MILLISECONDS);

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
                Log.e(TAG, "Error updating streak: " + e.getMessage());
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        } else {
            // No progress record exists yet, create one with streak of 1
            ContentValues values = new ContentValues();
            values.put(COLUMN_USER_ID, userId);
            values.put(COLUMN_STREAK, 1);
            values.put(COLUMN_LAST_STUDY_DATE, getCurrentDate());
            values.put(COLUMN_PROGRESS_ID, generateUniqueProgressId(db));
            // Add other required fields with default values
            values.put(COLUMN_DIFFICULTY_LEVEL, 1);
            values.put(COLUMN_COMPLETION_TIME, getCurrentDate());
            values.put(COLUMN_XP, 0);
            values.put(COLUMN_LESSON_ID, 1); // Default lesson ID

            db.insert(TABLE_USER_PROGRESS, null, values);
            Log.d(TAG, "Created new streak record for user: " + userId);
        }
    }

    public int getUserXp(Context context) {
    SessionManager sessionManager = new SessionManager(context); // Use the passed context
    int userId = Integer.parseInt(sessionManager.getUserDetails().get("userId"));
    if (userId >= 0) {
        Cursor cursor = this.getUserProgress(userId);
        if (cursor != null && cursor.moveToFirst()) {
            try {
                return cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_XP));
            } catch (Exception e) {
                Log.e(TAG, "Can not get user XP", e); // Log the full exception
            } finally {
                cursor.close();
            }
        }
    }
    return 0; // Return 0 instead of -1 for better default behavior
}

public int getUserStreak(Context context) {
    SessionManager sessionManager = new SessionManager(context); // Use the passed context
    int userId = Integer.parseInt(sessionManager.getUserDetails().get("userId"));
    if (userId >= 0) {
        Cursor cursor = this.getUserProgress(userId);
        if (cursor != null && cursor.moveToFirst()) {
            try {
                return cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STREAK));
            } catch (Exception e) {
                Log.e(TAG, "Can not get user streak", e); // Log the full exception
            } finally {
                cursor.close();
            }
        }
    }
    return 0; // Return 0 instead of -1 for better default behavior
}

    /**
     * Get the current user's username from SharedPreferences
     * @param context The application context
     * @return The current username or null if not logged in
     */
    public String getCurrentUsername(Context context) {
        // Get the stored user ID from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("current_user_id", -1);

        if (currentUserId == -1) {
            Log.d(TAG, "Can not get user id");
            return null; // No user is logged in
        }

        // Use the existing method to get username by ID
        SQLiteDatabase db = this.getReadableDatabase();
        String username = null;

        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_NAME},
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(currentUserId)},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
            cursor.close();
        } else {
            Log.d(TAG, "Username cursor is NULL");
        }

        return username;
    }

   public String getUsernameById(int userId) {
       SQLiteDatabase db = this.getReadableDatabase();
       Cursor cursor = db.query(
           TABLE_USERS,
           new String[]{COLUMN_NAME},
           COLUMN_USER_ID + "=?",
           new String[]{String.valueOf(userId)},
           null,
           null,
           null
       );

       String username = null;
       if (cursor != null && cursor.moveToFirst()) {
           username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
           cursor.close();
       }
       return username;
   }

    // Other methods and database setup...

    public String getLessonTitle(Context context) {
        // Query the database to get the lesson title
        return "Sample Lesson Title"; // Replace with actual query result
    }

    public String getLessonDescription(Context context) {
        // Query the database to get the lesson description
        return "Sample Lesson Description"; // Replace with actual query result
    }

    public String getLessonContent(Context context) {
        // Query the database to get the lesson content
        return "Sample Lesson Content"; // Replace with actual query result
    }

    public void insertVocabulary(SQLiteDatabase db, String word, String pronunciation, int wordId, int lessonId) {
        try {
            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put(COLUMN_WORD, word);
            values.put(COLUMN_PRONUNCIATION, pronunciation);
            values.put(COLUMN_WORD_ID, wordId);
            values.put(COLUMN_LESSON_ID, lessonId);

            long result = db.insert(TABLE_VOCABULARY, null, values);

            if (result == -1) {
                throw new SQLException("Failed to insert vocabulary: " + word);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error on insertVocabulary: word=" + word + ", wordId=" + wordId, e);
        } finally {
            db.endTransaction();
        }
    }
    private void insertDefaultLessons(SQLiteDatabase db) {
        String[][] lessonsData = {
                {"1", "Basic Greetings", "Learn common greeting phrases for daily use.", "1"},
                {"2", "Daily Activities", "Vocabulary for everyday actions and routines.", "1"},
                {"3", "Travel", "Essential words for traveling and navigation.", "2"},
                {"4", "Business Meetings", "Terms used in professional meeting settings.", "2"},
                {"5", "Research", "Vocabulary for academic and research purposes.", "3"}
        };

        try {
            db.beginTransaction();
            for (String[] lesson : lessonsData) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_LESSON_ID, Integer.parseInt(lesson[0]));
                values.put(COLUMN_TOPIC, lesson[1]);
                values.put(COLUMN_LESSON_CONTENT, lesson[2]);
                values.put(COLUMN_DIFFICULTY_LEVEL, Integer.parseInt(lesson[3]));
                db.insert(TABLE_LESSONS, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default lessons: ", e);
        } finally {
            db.endTransaction();
        }
    }
    private void insertDefaultVocabulary(SQLiteDatabase db) {
        String[][] vocabularyData = {
                {"Hello", "/həˈloʊ/", "1", "1"},
                {"Goodbye", "/ɡʊdˈbaɪ/", "2", "1"},
                {"Thank you", "/θæŋk juː/", "3", "1"},
                {"Airport", "/ˈɛrpɔːrt/", "7", "3"},
                {"Hotel", "/hoʊˈtɛl/", "8", "3"}
        };

        try {
            db.beginTransaction();
            for (String[] vocab : vocabularyData) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_WORD, vocab[0]);
                values.put(COLUMN_PRONUNCIATION, vocab[1]);
                values.put(COLUMN_WORD_ID, Integer.parseInt(vocab[2]));
                values.put(COLUMN_LESSON_ID, Integer.parseInt(vocab[3]));
                db.insert(TABLE_VOCABULARY, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default vocabulary: ", e);
        } finally {
            db.endTransaction();
        }
    }

    private void initializeLessonAndVocabulary(SQLiteDatabase db) {
        insertDefaultLessons(db);
        insertDefaultVocabulary(db);

    }

    public Lesson getLesson(Context context, int lessonId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Lesson lesson = null;

        Cursor cursor = null;
        try {
            cursor = db.query(
                    TABLE_LESSONS,
                    new String[]{COLUMN_LESSON_ID, COLUMN_TOPIC, COLUMN_LESSON_CONTENT, COLUMN_DIFFICULTY_LEVEL},
                    COLUMN_LESSON_ID + "=?",
                    new String[]{String.valueOf(lessonId)},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LESSON_ID));
                String topic = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TOPIC));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LESSON_CONTENT));
                int difficultyLevel = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DIFFICULTY_LEVEL));

                lesson = new Lesson(id, topic, content, difficultyLevel);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving lesson with ID: " + lessonId, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return lesson;
    }
    // LearningAppDatabase.java
    public List<String> getVocabularyByLessonId(int lessonId) {
        List<String> vocabularyList;
        vocabularyList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(
                    TABLE_VOCABULARY,
                    new String[]{COLUMN_WORD},
                    COLUMN_LESSON_ID + "=?",
                    new String[]{String.valueOf(lessonId)},
                    null,
                    null,
                    COLUMN_WORD_ID
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String word = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORD));
                    vocabularyList.add(word);
                } while (cursor.moveToNext());
            }

            Log.d(TAG, "Get vocabulary successful for lessonId: " + lessonId);

        } catch (Exception e) {
            Log.e(TAG, "Cannot get vocabulary for lessonId: " + lessonId, e);
            return null;

        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return vocabularyList;
    }

}
