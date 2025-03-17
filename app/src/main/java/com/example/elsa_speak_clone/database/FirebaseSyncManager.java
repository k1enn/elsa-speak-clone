package com.example.elsa_speak_clone.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class FirebaseSyncManager  {
    private static final String TAG = "FirebaseSyncManager";
    private final LearningAppDatabase localDb;
    private final FirebaseDatabase firebaseDb;
    private final DatabaseReference dbRef;
    private final Context context;

    public FirebaseSyncManager(Context context) {
        this.context = context;
        this.localDb = new LearningAppDatabase(context);
        this.firebaseDb = FirebaseDatabase.getInstance();
        this.dbRef = firebaseDb.getReference();
    }

    public void syncUserData(int userId) {
        // Get local user data
        SQLiteDatabase db = localDb.getReadableDatabase();
        Cursor cursor = db.query(
                LearningAppDatabase.TABLE_USERS,
                null,  // all columns
                LearningAppDatabase.COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            // Create user model from cursor
            HashMap<String, Object> userData = new HashMap<>();
            userData.put("userId", cursor.getInt(cursor.getColumnIndexOrThrow(LearningAppDatabase.COLUMN_USER_ID)));
            userData.put("name", cursor.getString(cursor.getColumnIndexOrThrow(LearningAppDatabase.COLUMN_NAME)));
            userData.put("gmail", cursor.getString(cursor.getColumnIndexOrThrow(LearningAppDatabase.COLUMN_GMAIL)));
            userData.put("joinDate", cursor.getString(cursor.getColumnIndexOrThrow(LearningAppDatabase.COLUMN_JOIN_DATE)));
            userData.put("isGoogleUser", cursor.getInt(cursor.getColumnIndexOrThrow(LearningAppDatabase.COLUMN_IS_GOOGLE_USER)));

            // Don't sync passwords for security reasons
            // userData.put("password", cursor.getString(cursor.getColumnIndexOrThrow(LearningAppDatabase.COLUMN_PASSWORD)));

            // Upload to Firebase
            dbRef.child("users").child(String.valueOf(userId)).setValue(userData)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User data synced successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error syncing user data: " + e.getMessage()));

            cursor.close();
        }
        db.close();
    }
    public void syncUserProgress(int userId) {
        // Get user progress from local DB
        SQLiteDatabase db = localDb.getReadableDatabase();
        Cursor cursor = localDb.getUserProgress(userId);

        if (cursor != null && cursor.moveToFirst()) {
            ArrayList<HashMap<String, Object>> progressList = new ArrayList<>();

            do {
                HashMap<String, Object> progressData = new HashMap<>();
                progressData.put("progressId", cursor.getInt(cursor.getColumnIndexOrThrow(LearningAppDatabase.COLUMN_PROGRESS_ID)));
                progressData.put("lessonId", cursor.getInt(cursor.getColumnIndexOrThrow(LearningAppDatabase.COLUMN_LESSON_ID)));
                progressData.put("difficultyLevel", cursor.getInt(cursor.getColumnIndexOrThrow(LearningAppDatabase.COLUMN_DIFFICULTY_LEVEL)));
                progressData.put("completionTime", cursor.getString(cursor.getColumnIndexOrThrow(LearningAppDatabase.COLUMN_COMPLETION_TIME)));
                progressData.put("streak", cursor.getInt(cursor.getColumnIndexOrThrow(LearningAppDatabase.COLUMN_STREAK)));
                progressData.put("xp", cursor.getInt(cursor.getColumnIndexOrThrow(LearningAppDatabase.COLUMN_XP)));
                progressData.put("lastStudyDate", cursor.getString(cursor.getColumnIndexOrThrow(LearningAppDatabase.COLUMN_LAST_STUDY_DATE)));

                progressList.add(progressData);
            } while (cursor.moveToNext());

            // Upload to Firebase
            dbRef.child("userProgress").child(String.valueOf(userId)).setValue(progressList)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User progress synced successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error syncing user progress: " + e.getMessage()));

            cursor.close();
        }
        db.close();
    }
    public void listenForRemoteChanges(int userId) {
        // Listen for user progress changes
        dbRef.child("userProgress").child(String.valueOf(userId))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Update local database with remote data
                        for (DataSnapshot progressSnapshot : dataSnapshot.getChildren()) {
                            // Extract data
                            int progressId = progressSnapshot.child("progressId").getValue(Integer.class);
                            int lessonId = progressSnapshot.child("lessonId").getValue(Integer.class);
                            int streak = progressSnapshot.child("streak").getValue(Integer.class);
                            int xp = progressSnapshot.child("xp").getValue(Integer.class);
                            String lastStudyDate = progressSnapshot.child("lastStudyDate").getValue(String.class);

                            // Update local database
                            updateLocalUserProgress(userId, progressId, lessonId, streak, xp, lastStudyDate);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Firebase progress sync failed: " + databaseError.getMessage());
                    }
                });
    }

    private void updateLocalUserProgress(int userId, int progressId, int lessonId, int streak, int xp, String lastStudyDate) {
        SQLiteDatabase db = localDb.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LearningAppDatabase.COLUMN_STREAK, streak);
        values.put(LearningAppDatabase.COLUMN_XP, xp);
        values.put(LearningAppDatabase.COLUMN_LAST_STUDY_DATE, lastStudyDate);

        db.update(
                LearningAppDatabase.TABLE_USER_PROGRESS,
                values,
                LearningAppDatabase.COLUMN_PROGRESS_ID + "=?",
                new String[]{String.valueOf(progressId)}
        );
        db.close();
    }
    public void setupOfflineCapability() {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        dbRef.keepSynced(true);
    }


}
