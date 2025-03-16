package com.example.elsa_speak_clone.database;


import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import java.sql.SQLException;
import java.util.HashMap;
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

import com.example.elsa_speak_clone.classes.Lesson;

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
    private static final String COLUMN_IS_GOOGLE_USER = "google";
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
            COLUMN_IS_GOOGLE_USER + " INTEGER, " +
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
        insertDefaultQuizzes(db);
        Log.d(TAG, "Insert QUIZZES");

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
      addGoogleColumnIfNeeded(db);
    }

      /**
     * Authenticate a user for login
     * @param usernameOrEmail The username (local auth) or email (Google auth)
     * @param password Password (not used for Google auth)
     * @param isGoogleAuth Flag indicating if this is a Google authentication attempt
     * @return True if authentication was successful
     */
      public boolean authenticateUser(String usernameOrEmail, String password, boolean isGoogleAuth) {
          if (usernameOrEmail == null || usernameOrEmail.isEmpty()) {
              return false;
          }

          // For Google auth, password can be empty
          if (!isGoogleAuth && (password == null || password.isEmpty())) {
              return false;
          }

          SQLiteDatabase db = this.getReadableDatabase();
          SessionManager sessionManager = new SessionManager(this.context);

          try {
              String selection;
              String[] selectionArgs;

              if (isGoogleAuth) {
                  // For Google auth, we check by email
                  selection = COLUMN_GMAIL + "=?";
                  selectionArgs = new String[]{usernameOrEmail};
              } else {
                  // For local auth, we check by username
                  selection = COLUMN_NAME + "=?";
                  selectionArgs = new String[]{usernameOrEmail};
              }

              try (Cursor cursor = db.query(
                      TABLE_USERS,
                      new String[]{COLUMN_USER_ID, COLUMN_PASSWORD, COLUMN_NAME, COLUMN_GMAIL, "google"},
                      selection,
                      selectionArgs,
                      null, null, null)) {

                  if (cursor.moveToFirst()) {
                      int userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
                      String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
                      String username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                      String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GMAIL));
                      int isGoogleUser = cursor.getInt(cursor.getColumnIndexOrThrow("google"));

                      if (isGoogleAuth) {
                          // For Google auth, we just check if the user exists
                          if (isGoogleUser == 1) {
                              // Create Google session
                              sessionManager.createGoogleSession(username, userId);
                              return true;
                          } else {
                              // This email exists but not as a Google user
                              Log.d(TAG, "Attempt to use Google auth for non-Google account");
                              return false;
                          }
                      } else {
                          // For local auth, we check password
                          try {
                              boolean passwordValidate = BCrypt.checkpw(password, storedPassword);
                              if (passwordValidate) {
                                  // Create local session
                                  sessionManager.createSession(username, userId);
                                  return true;
                              }
                          } catch (Exception e) {
                              Log.d(TAG, "Can not authenticate password", e);
                          }
                      }
                  }
                  return false;
              }
          } catch (Exception e) {
              Log.e(TAG, "Error authenticating user: " + e.getMessage(), e);
              return false;
          }
      }


    public boolean authenticateGoogleUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        SessionManager sessionManager = new SessionManager(this.context);
        Cursor cursor = null;

        try {
            String selection = COLUMN_GMAIL + "=?";
            String[] selectionArgs = {email};

            cursor = db.query(
                    TABLE_USERS,
                    new String[]{COLUMN_USER_ID, COLUMN_NAME, "google"},
                    selection,
                    selectionArgs,
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") int userId = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID));
                @SuppressLint("Range") String username = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                @SuppressLint("Range") int isGoogleUser = cursor.getInt(cursor.getColumnIndex("google"));

                if (isGoogleUser == 1) {
                    // Create Google session
                    sessionManager.createGoogleSession(username, userId);
                    return true;
                } else {
                    // This email exists but not as a Google user
                    Log.d(TAG, "Email exists but not as Google account: " + email);
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error authenticating Google user", e);
            return false;
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    public boolean authenticateLocalUser(String email, String password) {
        return authenticateUser(email, password, false);
    }
    public boolean registerGoogleUser(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            // Extract username from email
            String username = email.split("@")[0];

            values.put(COLUMN_GMAIL, email);
            values.put(COLUMN_NAME, username);
            values.put(COLUMN_PASSWORD, ""); // Empty password for Google users
            values.put("google", 1); // Mark as Google user
            values.put(COLUMN_JOIN_DATE, getCurrentDate());

            long result = db.insert(TABLE_USERS, null, values);
            db.close();

            return result != -1;
        } catch (Exception e) {
            Log.e("DB_ERROR", "Failed to register Google user: " + e.getMessage());
            db.close();
            return false;
        }
    }



    public boolean registerLocalUser(String username, String password) {
        return registerUser(username, password, false);
    }


    /**
     * Register a new user with proper differentiation between local and Google accounts
     * @param username The username (or email for Google users)
     * @param password Password (empty for Google users)
     * @param isGoogleUser Boolean flag to identify Google authentication
     * @return True if registration was successful
     */
    public boolean registerUser(String username, String password, boolean isGoogleUser) {
        if (username == null) return false;

        SQLiteDatabase db = this.getWritableDatabase();

        // First ensure the google column exists
        addGoogleColumnIfNeeded(db);

        ContentValues values = new ContentValues();
        int userId = -1;

        try {
            userId = generateUniqueId(db);
            values.put(COLUMN_USER_ID, userId);
            Log.d(TAG, "User ID = " + userId);

            if (isGoogleUser) {
                // Google authentication
                String displayName = username.split("@")[0]; // Extract username from email
                values.put(COLUMN_GMAIL, username); // Store full email
                values.put(COLUMN_NAME, displayName);
                values.put(COLUMN_PASSWORD, ""); // Empty password for Google users
                values.put("google", 1); // Use the actual column name in the database
            } else {
                // Local authentication
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                values.put(COLUMN_NAME, username);
                values.put(COLUMN_GMAIL, emptyString);
                values.put(COLUMN_PASSWORD, hashedPassword);
                values.put("google", 0); // Use the actual column name in the database
            }

            values.put(COLUMN_JOIN_DATE, getCurrentDate());

            long result = db.insert(TABLE_USERS, null, values);

            if (result != -1) {
                // Only create session after successful database insertion
                SessionManager sessionManager = new SessionManager(this.context);
                if (isGoogleUser) {
                    sessionManager.createGoogleSession(username.split("@")[0], userId);
                } else {
                    sessionManager.createSession(username, userId);
                }
                return true;
            } else {
                Log.e(TAG, "Failed to insert user into database");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error registering user: " + e.getMessage(), e);
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
    /**
     * Update user's streak based on login date
     * @param context The application context to access SharedPreferences
     */
    public void updateUserStreak(Context context) {
        try {
            SessionManager sessionManager = new SessionManager(context);

            // Get user details in a safer way
            HashMap<String, String> userDetails = sessionManager.getUserDetails();
            String userIdStr = userDetails.get(SessionManager.KEY_USER_ID);

            if (userIdStr == null || userIdStr.isEmpty()) {
                Log.e(TAG, "Cannot update streak: User ID not found in session");
                return;
            }

            int userId;
            try {
                userId = Integer.parseInt(userIdStr);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Cannot update streak: Invalid user ID format: " + userIdStr, e);
                return;
            }

            if (userId < 0) {
                Log.e(TAG, "Cannot update streak: Invalid user ID value: " + userId);
                return;
            }

            // Log the user ID for debugging
            Log.d(TAG, "Updating streak for user ID: " + userId);

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = getUserProgress(userId);

            if (cursor != null && cursor.moveToFirst()) {
                try {
                    // Get last study date and current streak
                    int lastStudyDateIndex = cursor.getColumnIndex(COLUMN_LAST_STUDY_DATE);
                    int streakIndex = cursor.getColumnIndex(COLUMN_STREAK);

                    if (lastStudyDateIndex == -1 || streakIndex == -1) {
                        Log.e(TAG, "Column not found in cursor. LAST_STUDY_DATE index: " +
                                lastStudyDateIndex + ", STREAK index: " + streakIndex);
                        cursor.close();
                        return;
                    }

                    String lastStudyDateStr = cursor.getString(lastStudyDateIndex);
                    int currentStreak = cursor.getInt(streakIndex);

                    // Get today's date
                    String todayStr = getCurrentDate();
                    Log.d(TAG, "Last study date: " + lastStudyDateStr + ", Today: " + todayStr);

                    // Convert last study date to Date object
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date lastStudyDate;
                    Date today;
                    try {
                        lastStudyDate = sdf.parse(lastStudyDateStr);
                        today = sdf.parse(todayStr);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing date in updateUserStreak", e);
                        cursor.close();
                        return;
                    }

                    // Calculate difference in days
                    long diff = TimeUnit.DAYS.convert(
                            today.getTime() - lastStudyDate.getTime(), TimeUnit.MILLISECONDS);
                    Log.d(TAG, "Days since last study: " + diff);

                    if (diff == 0) {
                        // Already updated today, no change needed
                        Log.d(TAG, "Streak already updated today, no change");
                        cursor.close();
                        return;
                    } else if (diff == 1) {
                        // Studied yesterday → Increment streak
                        currentStreak++;
                        Log.d(TAG, "Incrementing streak to: " + currentStreak);
                    } else if (diff > 1) {
                        // Missed a day → Reset streak
                        currentStreak = 1;
                        Log.d(TAG, "Resetting streak to 1");
                    }

                    // Update the database
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_STREAK, currentStreak);
                    values.put(COLUMN_LAST_STUDY_DATE, todayStr); // Update last study date to today

                    int rowsUpdated = db.update(TABLE_USER_PROGRESS, values,
                            COLUMN_USER_ID + " = ?",
                            new String[]{String.valueOf(userId)});
                    Log.d(TAG, "Updated " + rowsUpdated + " rows in user progress table");

                } catch (Exception e) {
                    Log.e(TAG, "Error updating streak: " + e.getMessage(), e);
                } finally {
                    cursor.close();
                }
            } else {
                // No progress record exists yet, create one with streak of 1
                try {
                    Log.d(TAG, "No existing progress record, creating new one with streak of 1");
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_USER_ID, userId);
                    values.put(COLUMN_STREAK, 1);
                    values.put(COLUMN_LAST_STUDY_DATE, getCurrentDate());

                    // Generate a unique progress ID
                    int progressId = generateUniqueProgressId(db);
                    values.put(COLUMN_PROGRESS_ID, progressId);

                    // Add other required fields with default values
                    values.put(COLUMN_DIFFICULTY_LEVEL, 1);
                    values.put(COLUMN_COMPLETION_TIME, getCurrentDate());
                    values.put(COLUMN_XP, 0);
                    values.put(COLUMN_LESSON_ID, 1); // Default lesson ID

                    long newRowId = db.insert(TABLE_USER_PROGRESS, null, values);
                    Log.d(TAG, "Created new streak record for user: " + userId +
                            ", new row ID: " + newRowId);
                } catch (Exception e) {
                    Log.e(TAG, "Error creating new progress record: " + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in updateUserStreak: " + e.getMessage(), e);
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
                {"2", "Family Members", "Vocabulary related to family relationships.", "1"},
                {"3", "Food & Dining", "Essential words for ordering and discussing food.", "1"},
                {"4", "Travel Essentials", "Common phrases and words for travelers.", "2"},
                {"5", "Shopping", "Vocabulary for shopping and transactions.", "2"},
                {"6", "Weather & Seasons", "Terms for describing weather conditions and seasons.", "2"},
                {"7", "Business Communication", "Professional vocabulary for workplace settings.", "3"},
                {"8", "Academic Language", "Terms used in educational and research contexts.", "3"},
                {"9", "Technology & Internet", "Modern vocabulary for digital communication.", "3"}
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
                // Lesson 1: Basic Greetings
                {"Hello", "/həˈloʊ/", "1", "1"},
                {"Goodbye", "/ɡʊdˈbaɪ/", "2", "1"},
                {"Thank you", "/θæŋk juː/", "3", "1"},
                {"Please", "/pliːz/", "4", "1"},
                {"Good morning", "/ɡʊd ˈmɔːrnɪŋ/", "5", "1"},
                {"Good evening", "/ɡʊd ˈiːvnɪŋ/", "6", "1"},
                {"How are you", "/haʊ ɑːr juː/", "7", "1"},
                {"Nice to meet you", "/naɪs tuː miːt juː/", "8", "1"},
                {"Excuse me", "/ɪkˈskjuːz miː/", "9", "1"},
                {"Sorry", "/ˈsɒri/", "10", "1"},

                // Lesson 2: Family Members
                {"Mother", "/ˈmʌðər/", "11", "2"},
                {"Father", "/ˈfɑːðər/", "12", "2"},
                {"Sister", "/ˈsɪstər/", "13", "2"},
                {"Brother", "/ˈbrʌðər/", "14", "2"},
                {"Grandmother", "/ˈɡrænˌmʌðər/", "15", "2"},
                {"Grandfather", "/ˈɡrænˌfɑːðər/", "16", "2"},
                {"Aunt", "/ænt/", "17", "2"},
                {"Uncle", "/ˈʌŋkəl/", "18", "2"},
                {"Cousin", "/ˈkʌzən/", "19", "2"},
                {"Child", "/tʃaɪld/", "20", "2"},

                // Lesson 3: Food & Dining
                {"Restaurant", "/ˈrestərɑːnt/", "21", "3"},
                {"Menu", "/ˈmenjuː/", "22", "3"},
                {"Breakfast", "/ˈbrekfəst/", "23", "3"},
                {"Lunch", "/lʌntʃ/", "24", "3"},
                {"Dinner", "/ˈdɪnər/", "25", "3"},
                {"Delicious", "/dɪˈlɪʃəs/", "26", "3"},
                {"Water", "/ˈwɔːtər/", "27", "3"},
                {"Bill", "/bɪl/", "28", "3"},
                {"Vegetarian", "/ˌvedʒəˈteriən/", "29", "3"},
                {"Spicy", "/ˈspaɪsi/", "30", "3"},

                // Lesson 4: Travel Essentials
                {"Airport", "/ˈɛrpɔːrt/", "31", "4"},
                {"Passport", "/ˈpæspɔːrt/", "32", "4"},
                {"Hotel", "/hoʊˈtɛl/", "33", "4"},
                {"Ticket", "/ˈtɪkɪt/", "34", "4"},
                {"Luggage", "/ˈlʌɡɪdʒ/", "35", "4"},
                {"Reservation", "/ˌrezərˈveɪʃən/", "36", "4"},
                {"Departure", "/dɪˈpɑːrtʃər/", "37", "4"},
                {"Arrival", "/əˈraɪvəl/", "38", "4"},
                {"Tourist", "/ˈtʊrɪst/", "39", "4"},
                {"Direction", "/dəˈrɛkʃən/", "40", "4"},

                // Lesson 5: Shopping
                {"Store", "/stɔːr/", "41", "5"},
                {"Price", "/praɪs/", "42", "5"},
                {"Discount", "/ˈdɪskaʊnt/", "43", "5"},
                {"Size", "/saɪz/", "44", "5"},
                {"Cash", "/kæʃ/", "45", "5"},
                {"Credit card", "/ˈkrɛdɪt kɑːrd/", "46", "5"},
                {"Receipt", "/rɪˈsiːt/", "47", "5"},
                {"Sale", "/seɪl/", "48", "5"},
                {"Try on", "/traɪ ɒn/", "49", "5"},
                {"Expensive", "/ɪkˈspɛnsɪv/", "50", "5"},

                // Lesson 6: Weather & Seasons
                {"Sunny", "/ˈsʌni/", "51", "6"},
                {"Rainy", "/ˈreɪni/", "52", "6"},
                {"Cloudy", "/ˈklaʊdi/", "53", "6"},
                {"Snow", "/snoʊ/", "54", "6"},
                {"Temperature", "/ˈtɛmprətʃər/", "55", "6"},
                {"Forecast", "/ˈfɔːrkæst/", "56", "6"},
                {"Summer", "/ˈsʌmər/", "57", "6"},
                {"Winter", "/ˈwɪntər/", "58", "6"},
                {"Autumn", "/ˈɔːtəm/", "59", "6"},
                {"Spring", "/sprɪŋ/", "60", "6"},

                // Lesson 7: Business Communication
                {"Meeting", "/ˈmiːtɪŋ/", "61", "7"},
                {"Presentation", "/ˌprezənˈteɪʃən/", "62", "7"},
                {"Deadline", "/ˈdɛdˌlaɪn/", "63", "7"},
                {"Conference", "/ˈkɒnfərəns/", "64", "7"},
                {"Negotiate", "/nɪˈɡoʊʃieɪt/", "65", "7"},
                {"Contract", "/ˈkɒntrækt/", "66", "7"},
                {"Colleague", "/ˈkɒliːɡ/", "67", "7"},
                {"Schedule", "/ˈʃɛdjuːl/", "68", "7"},
                {"Budget", "/ˈbʌdʒɪt/", "69", "7"},
                {"Report", "/rɪˈpɔːrt/", "70", "7"},

                // Lesson 8: Academic Language
                {"Research", "/rɪˈsɜːrtʃ/", "71", "8"},
                {"Thesis", "/ˈθiːsɪs/", "72", "8"},
                {"Analysis", "/əˈnæləsɪs/", "73", "8"},
                {"Theory", "/ˈθɪəri/", "74", "8"},
                {"Bibliography", "/ˌbɪbliˈɒɡrəfi/", "75", "8"},
                {"Citation", "/saɪˈteɪʃən/", "76", "8"},
                {"Hypothesis", "/haɪˈpɒθəsɪs/", "77", "8"},
                {"Methodology", "/ˌmɛθəˈdɒlədʒi/", "78", "8"},
                {"Conclusion", "/kənˈkluːʒən/", "79", "8"},
                {"Abstract", "/ˈæbstrækt/", "80", "8"},

                // Lesson 9: Technology & Internet
                {"Website", "/ˈwɛbˌsaɪt/", "81", "9"},
                {"Download", "/ˌdaʊnˈloʊd/", "82", "9"},
                {"Password", "/ˈpæsˌwɜːrd/", "83", "9"},
                {"Software", "/ˈsɒftˌwɛr/", "84", "9"},
                {"Hardware", "/ˈhɑːrdˌwɛr/", "85", "9"},
                {"Wireless", "/ˈwaɪərləs/", "86", "9"},
                {"Database", "/ˈdeɪtəˌbeɪs/", "87", "9"},
                {"Algorithm", "/ˈælɡəˌrɪðəm/", "88", "9"},
                {"Encryption", "/ɪnˈkrɪpʃən/", "89", "9"},
                {"Cloud", "/klaʊd/", "90", "9"}
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
    private void insertDefaultQuizzes(SQLiteDatabase db) {
        String[][] quizzesData = {
                // Format: {"QuizId", "Question", "Answer", "LessonId"}
                // Lesson 1: Basic Greetings
                {"1", "How ___ you?", "are", "1"},
                {"2", "What ___ your name?", "is", "1"},
                {"3", "Good ___, how are you?", "morning", "1"},
                {"4", "I'm ___ to meet you.", "pleased", "1"},
                {"5", "You should greet Dr. Smith ___ when meeting for the first time.", "formally", "1"},
                {"6", "When meeting someone for the first time, you should use a ___ greeting.", "formal", "1"},
                {"7", "It's appropriate to use ___ titles when greeting professionals.", "professional", "1"},
                {"8", "Good ___ is a formal greeting used in the evening.", "evening", "1"},
                {"9", "When greeting a female with unknown marital status, use ___.", "Ms", "1"},
                {"10", "___ you later is an informal way to say goodbye.", "See", "1"},

                // Lesson 2: Family Members
                {"11", "My mother's daughter is my ___.", "sister", "2"},
                {"12", "My mother's mother is my ___.", "grandmother", "2"},
                {"13", "My father's son is my ___.", "brother", "2"},
                {"14", "My aunt's children are my ___.", "cousins", "2"},
                {"15", "My sister's husband is my ___.", "brother-in-law", "2"},
                {"16", "My brother's daughter is my ___.", "niece", "2"},
                {"17", "My dad's brother is my ___.", "uncle", "2"},
                {"18", "My grandpa's father is my ___.", "great grandpa", "2"},
                {"19", "My female spouse is my ___.", "wife", "2"},
                {"20", "My step-mother's son is my ___.", "step-brother", "2"},

                // Lesson 3: Food & Dining
                {"21", "A place where you buy bread and cakes is called a ___.", "bakery", "3"},
                {"22", "The ___ shows all available food options in a restaurant.", "menu", "3"},
                {"23", "The first meal of the day is called ___.", "breakfast", "3"},
                {"24", "When food tastes very good, it is ___.", "delicious", "3"},
                {"25", "A person who doesn't eat meat is called a ___.", "vegetarian", "3"},
                {"26", "Food with a hot, pungent flavor is described as ___.", "spicy", "3"},
                {"27", "The ___ is what you ask for when you want to pay in a restaurant.", "bill", "3"},
                {"28", "The evening meal is commonly called ___.", "dinner", "3"},
                {"29", "The midday meal is called ___.", "lunch", "3"},
                {"30", "A ___ is a place where you buy food and household items.", "grocery store", "3"},

                // Lesson 4: Travel Essentials
                {"31", "I need to ___ a flight for my vacation.", "book", "4"},
                {"32", "The plane ___ at 3 PM.", "departs", "4"},
                {"33", "Can I have a window ___?", "seat", "4"},
                {"34", "Where is the baggage ___?", "claim", "4"},
                {"35", "You need a ___ to travel internationally.", "passport", "4"},
                {"36", "A ___ is a place where planes land and take off.", "airport", "4"},
                {"37", "You need to make a ___ to secure your hotel room.", "reservation", "4"},
                {"38", "Your ___ contains your clothes and personal items for travel.", "luggage", "4"},
                {"39", "The ___ is the tallest mountain in the world.", "Mount Everest", "4"},
                {"40", "The Nile is the ___ river in the world.", "longest", "4"},

                // Lesson 5: Shopping
                {"41", "A ___ is a shop where you buy medicine.", "pharmacy", "4"},
                {"42", "A ___ store is where you buy computers and TVs.", "electronics", "5"},
                {"43", "A place to buy rings and necklaces is a ___ store.", "jewelry", "5"},
                {"44", "The ___ of this item is too high.", "price", "5"},
                {"45", "Can I get a ___ on this item?", "discount", "5"},
                {"46", "I need to ___ these clothes before buying them.", "try on", "5"},
                {"47", "This item is on ___, so it costs less than usual.", "sale", "5"},
                {"48", "I'll pay with my ___ card instead of cash.", "credit", "5"},
                {"49", "Please give me a ___ for my purchase.", "receipt", "5"},
                {"50", "This product is very ___; it costs a lot of money.", "expensive", "5"},

                // Lesson 6: Weather & Seasons
                {"51", "Today is a ___ day with no clouds in the sky.", "sunny", "6"},
                {"52", "Don't forget your umbrella, it's a ___ day.", "rainy", "6"},
                {"53", "The sky is ___ today, we can't see the sun.", "cloudy", "6"},
                {"54", "In winter, we often see ___ falling from the sky.", "snow", "6"},
                {"55", "The ___ today is 25 degrees Celsius.", "temperature", "6"},
                {"56", "The weather ___ says it will rain tomorrow.", "forecast", "6"},
                {"57", "The hottest season of the year is ___.", "summer", "6"},
                {"58", "The coldest season of the year is ___.", "winter", "6"},
                {"59", "Another name for fall is ___.", "autumn", "6"},
                {"60", "The season that comes after winter is ___.", "spring", "6"},

                // Lesson 7: Business Communication
                {"61", "Let's ___ the meeting at 9 AM.", "schedule", "7"},
                {"62", "Could you ___ the presentation with the team?", "share", "7"},
                {"63", "We need to ___ this issue in our meeting.", "discuss", "7"},
                {"64", "The ___ will be sent after the meeting.", "minutes", "7"},
                {"65", "We need to meet the project ___ by Friday.", "deadline", "7"},
                {"66", "I'll prepare a ___ for the board meeting.", "presentation", "7"},
                {"67", "We should ___ with the client about the terms.", "negotiate", "7"},
                {"68", "Please sign the ___ before we proceed.", "contract", "7"},
                {"69", "My ___ will attend the meeting in my place.", "colleague", "7"},
                {"70", "We need to stay within the ___ for this project.", "budget", "7"},

                // Lesson 8: Academic Language
                {"71", "The ___ shows interesting results.", "data", "8"},
                {"72", "We need to ___ more sources in our paper.", "cite", "8"},
                {"73", "This research ___ was published last year.", "paper", "8"},
                {"74", "The ___ supported our hypothesis.", "findings", "8"},
                {"75", "We should ___ the methodology carefully.", "review", "8"},
                {"76", "A ___ is the main argument of your research paper.", "thesis", "8"},
                {"77", "The ___ section summarizes your research paper.", "abstract", "8"},
                {"78", "We conducted an ___ of the experimental results.", "analysis", "8"},
                {"79", "The ___ lists all sources used in your paper.", "bibliography", "8"},
                {"80", "A ___ is an educated guess that can be tested.", "hypothesis", "8"},

                // Lesson 9: Technology & Internet
                {"81", "I need to ___ this file to my computer.", "download", "9"},
                {"82", "Create a strong ___ for your online accounts.", "password", "9"},
                {"83", "The company's ___ provides information about their services.", "website", "9"},
                {"84", "Computer programs are also called ___.", "software", "9"},
                {"85", "Physical computer components are called ___.", "hardware", "9"},
                {"86", "A ___ connection doesn't need cables.", "wireless", "9"},
                {"87", "A collection of organized information is called a ___.", "database", "9"},
                {"88", "An ___ is a step-by-step procedure for calculations.", "algorithm", "9"},
                {"89", "___ protects your data from unauthorized access.", "encryption", "9"},
                {"90", "Data stored on the ___ can be accessed from anywhere.", "cloud", "9"}
        };

        try {
            db.beginTransaction();
            for (String[] quiz : quizzesData) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_QUIZ_ID, Integer.parseInt(quiz[0]));
                values.put(COLUMN_QUESTION, quiz[1]);
                values.put(COLUMN_ANSWER, quiz[2]);
                values.put(COLUMN_LESSON_ID, Integer.parseInt(quiz[3]));
                db.insert(TABLE_QUIZZES, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error inserting default quizzes: ", e);
        } finally {
            Log.e(TAG, "Successful inserting default quizzes");
            db.endTransaction();
        }
    }

    /**
     * Get username by email
     * @param email The user's email
     * @return The username associated with this email
     */
    @SuppressLint("Range")
    public String getUsernameByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String username = null;

        String[] columns = {"username"};
        String selection = "email = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query("users", columns, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            username = cursor.getString(cursor.getColumnIndex("username"));
            cursor.close();
        }

        db.close();
        return username;
    }

    /**
     * Check if a Gmail account exists in the database
     * @param email The Gmail address to check
     * @return True if the email exists in the database
     */
    public boolean doesUserGmailExist(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = "Gmail = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query("users", null, selection, selectionArgs, null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;

        if (cursor != null) {
            cursor.close();
        }

        db.close();
        return exists;
    }
    public void addGoogleColumnIfNeeded(SQLiteDatabase db) {
        try {
            // Check if column exists first to avoid errors
            Cursor cursor = db.rawQuery("PRAGMA table_info('" + TABLE_USERS + "')", null);
            boolean columnExists = false;

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    @SuppressLint("Range") String columnName = cursor.getString(cursor.getColumnIndex("name"));
                    if ("google".equals(columnName)) {
                        columnExists = true;
                        break;
                    }
                }
                cursor.close();
            }

            if (!columnExists) {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN google INTEGER DEFAULT 0");
                Log.d(TAG, "Added 'google' column to Users table");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding google column: " + e.getMessage(), e);
        }
    }
    public int getUserIdByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        int userId = -1;
        Cursor cursor = null;

        try {
            String[] columns = {"UserId"};
            String selection = "Gmail = ?"; // This is correct based on your DB schema
            String[] selectionArgs = {email};

            cursor = db.query(
                    "Users",
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("UserId");
                if (columnIndex != -1) {
                    userId = cursor.getInt(columnIndex);
                }
            }
        } catch (Exception e) {
            Log.e("DB_ERROR", "Error getting user ID for email: " + email, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return userId;
    }


}
