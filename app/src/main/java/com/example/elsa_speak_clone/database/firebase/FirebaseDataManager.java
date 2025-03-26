package com.example.elsa_speak_clone.database.firebase;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.SessionManager;
import com.example.elsa_speak_clone.database.dao.UserProgressDao;
import com.example.elsa_speak_clone.database.entities.User;
import com.example.elsa_speak_clone.database.entities.UserProgress;
import com.example.elsa_speak_clone.database.repositories.UserProgressRepository;
import com.example.elsa_speak_clone.database.repositories.UserRepository;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;

public class FirebaseDataManager {
    private static final String TAG = "FirebaseDataManager";
    private static FirebaseDataManager instance;
    
    // Database references
    private final FirebaseDatabase database;
    private final DatabaseReference usersRef;
    private final DatabaseReference leaderboardRef;
    private final DatabaseReference usersTableRef;
    
    // Local data access
    private final Context context;
    private final UserRepository userRepository;
    private final UserProgressRepository progressRepository;
    private final SessionManager sessionManager;
    
    // Database paths
    private static final String USERS_PATH = "users";
    private static final String LEADERBOARD_PATH = "leaderboard";
    private static final String USERS_TABLE_PATH = "usersTable";
    
    private FirebaseDataManager(Context context) {
        this.context = context.getApplicationContext();
        this.database = FirebaseDatabase.getInstance();
        this.usersRef = database.getReference(USERS_PATH);
        this.leaderboardRef = database.getReference(LEADERBOARD_PATH);
        this.usersTableRef = database.getReference(USERS_TABLE_PATH);
        
        // Initialize local data access
        if (context.getApplicationContext() instanceof Application) {
            Application app = (Application) context.getApplicationContext();
            this.userRepository = new UserRepository(app);
            this.progressRepository = new UserProgressRepository(app);
        } else {
            Log.e(TAG, "Context is not from an Application - using application context");
            // Fallback in case the provided context is not from an Application
            Context appContext = context.getApplicationContext();
            if (appContext instanceof Application) {
                Application app = (Application) appContext;
                this.userRepository = new UserRepository(app);
                this.progressRepository = new UserProgressRepository(app);
            } else {
                throw new IllegalArgumentException("FirebaseDataManager requires an Application context");
            }
        }
        
        this.sessionManager = new SessionManager(context);
    }
    
    public static synchronized FirebaseDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseDataManager(context);
        }
        return instance;
    }

    // Update leaderboard when an event happened
    public CompletableFuture<Boolean> updateLeaderboard(String username, int userId, int streak, int xp) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        if (username == null || username.isEmpty()) {
            Log.e(TAG, "Cannot update leaderboard with empty username");
            future.complete(false);
            return future;
        }
        
        //  
        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("userId", Integer.valueOf(userId));
        userUpdate.put("userStreak", Integer.valueOf(streak));
        userUpdate.put("userXp", Integer.valueOf(xp));
        
        DatabaseReference userRef = leaderboardRef.child(username);
        
        userRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                // If this is a new entry, setValue creates it entirely
                if (mutableData.getValue() == null) {
                    mutableData.setValue(userUpdate);
                    return Transaction.success(mutableData);
                }
                
                // For existing entries, update each field individually
                mutableData.child("userId").setValue(userId);
                mutableData.child("userStreak").setValue(streak);
                mutableData.child("userXp").setValue(xp);
                
                return Transaction.success(mutableData);
            }
            
            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Log.e(TAG, "Firebase transaction failed: " + databaseError.getMessage(), databaseError.toException());
                    future.complete(false);
                    return;
                }
                
                if (committed) {
                    Log.d(TAG, "Leaderboard updated successfully for " + username);
                    Log.d(TAG, "New values - Streak: " + streak + ", XP: " + xp);
                    future.complete(true);
                } else {
                    Log.e(TAG, "Firebase transaction not committed for " + username);
                    future.complete(false);
                }
            }
        });
        
        return future;
    }
    
    /**
     * Get user progress from Firebase 
     */
    public CompletableFuture<Map<String, Object>> getUserProgressFromFirebase(String username) {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        leaderboardRef.child(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Map<String, Object> progressData = new HashMap<>();
                            for (DataSnapshot child : snapshot.getChildren()) {
                                progressData.put(child.getKey(), child.getValue());
                            }
                            future.complete(progressData);
                        } else {
                            future.complete(null);
                        }
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error getting user progress: " + error.getMessage());
                        future.complete(null);
                    }
                });
        
        return future;
    }
    
    /**
     * Sync local user progress to Firebase after quiz completion
     */
    public void syncUserProgress(int userId, int lessonId) {
        try {
            // First update local progress
            UserProgress progress = progressRepository.getUserLessonProgress(userId, lessonId);
            User user = userRepository.getUserById(userId);
            
            if (progress != null && user != null) {
                // Update local progress
                int currentXp = progress.getXp();
                int currentStreak = progress.getStreak();
                
                int newXp = currentXp;
                progress.setXp(newXp);
                progress.setStreak(currentStreak);
                
                // Update the progress in the database
                progressRepository.updateUserProgress(progress);
                
                // Get total XP
                int totalXp = getTotalUserXp(userId);
                
                // Update Firebase leaderboard
                updateLeaderboard(user.getName(), userId, currentStreak, totalXp)
                        .thenAccept(success -> {
                            if (success) {
                                Log.d(TAG, "Leaderboard synced to Firebase successfully");
                                
                                // Also update user table
                                updateUserStats(user.getName(), totalXp, currentStreak)
                                    .thenAccept(userUpdateSuccess -> {
                                        if (userUpdateSuccess) {
                                            Log.d(TAG, "User table synced to Firebase successfully");
                                        } else {
                                            Log.e(TAG, "Failed to sync user table to Firebase");
                                        }
                                    });
                            } else {
                                Log.e(TAG, "Failed to sync leaderboard to Firebase");
                            }
                        });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error syncing user progress: " + e.getMessage(), e);
        }
    }
    
    /**
     * Calculate total XP for a user across all lessons
     */
    private int getTotalUserXp(int userId) {
        List<UserProgress> progressList = progressRepository.getUserProgressList(userId);
        int totalXp = 0;
        
        if (progressList != null) {
            for (UserProgress progress : progressList) {
                totalXp += progress.getXp();
            }
        }
        
        return totalXp;
    }
    
   public CompletableFuture<Boolean> pullUserProgressToLocal(String username, int userId) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    
    if (username == null || username.isEmpty()) {
        Log.e(TAG, "Cannot pull progress with empty username");
        future.complete(false);
        return future;
    }
    
    Log.d(TAG, "Checking Firebase for user: " + username);
    
    // Check if username exists in Firebase userTable
    usersTableRef.child(username)
        .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "Found user in Firebase: " + username);
                    
                    // Get XP and streak values
                    Long userXp = snapshot.child("userXp").getValue(Long.class);
                    Long userStreak = snapshot.child("userStreak").getValue(Long.class);

                    
                    Log.d(TAG, "Firebase values - XP: " + userXp + ", Streak: " + userStreak);
                    
                    if (userXp != null && userStreak != null) {
                        // Direct database update using Room DAO
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            try {
                                // Get the UserProgressDao from the repository
                                UserProgressDao progressDao = progressRepository.getUserProgressDao();
                                
                                // Get all progress entries for this user
                                List<UserProgress> progressList = progressDao.getUserProgress(userId);
                                
                                if (progressList != null && !progressList.isEmpty()) {
                                    // Update all progress entries with the Firebase data
                                    for (UserProgress progress : progressList) {
                                        progress.setXp(Math.toIntExact(userXp));
                                        progress.setStreak(Math.toIntExact(userStreak));
                                        progressDao.update(progress);
                                    }
                                    
                                    Log.d(TAG, "Updated " + progressList.size() + " progress entries with XP: " + userXp + ", Streak: " + userStreak);
                                    future.complete(true);
                                } else {
                                    // No progress entries found, create a default one
                                    Log.d(TAG, "No progress entries found, creating default entry");
                                    UserProgress newProgress = new UserProgress();
                                    newProgress.setUserId(userId);
                                    newProgress.setLessonId(1); // Default lesson
                                    newProgress.setXp(Math.toIntExact(userXp));
                                    newProgress.setStreak(Math.toIntExact(userStreak));
                                    newProgress.setLastStudyDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
                                    
                                    progressDao.insert(newProgress);
                                    Log.d(TAG, "Created new progress entry with XP: " + userXp + ", Streak: " + userStreak);
                                    future.complete(true);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error updating database: " + e.getMessage(), e);
                                future.complete(false);
                            }
                        });
                    } else {
                        Log.d(TAG, "Missing XP or streak data in Firebase");
                        future.complete(false);
                    }
                } else {
                    Log.d(TAG, "Username not found in Firebase: " + username);
                    future.complete(false);
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase error: " + error.getMessage());
                future.complete(false);
            }
        });
    
    return future;
}
    private void debugUserProgress(int userId) {
    AppDatabase.databaseWriteExecutor.execute(() -> {
        try {
            UserProgressRepository repo = new UserProgressRepository(this.context);
            List<UserProgress> progressList = repo.getUserProgressList(userId);
            
            if (progressList != null && !progressList.isEmpty()) {
                for (UserProgress progress : progressList) {
                    Log.d(TAG, "DEBUG - Progress: userId=" + progress.getUserId() + 
                          ", lessonId=" + progress.getLessonId() + 
                          ", XP=" + progress.getXp() + 
                          ", Streak=" + progress.getStreak());
                }
            } else {
                Log.d(TAG, "DEBUG - No progress entries found for userId: " + userId);
            }
        } catch (Exception e) {
            Log.e(TAG, "DEBUG - Error checking progress: " + e.getMessage(), e);
        }
    });
}
    /**
     * Get top users for leaderboard
     */
    public CompletableFuture<Map<String, Map<String, Object>>> getTopUsers(int limit) {
        CompletableFuture<Map<String, Map<String, Object>>> future = new CompletableFuture<>();
        
        leaderboardRef.orderByChild("userXp")
                .limitToLast(limit)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, Map<String, Object>> topUsers = new HashMap<>();
                        
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String username = userSnapshot.getKey();
                            Map<String, Object> userData = new HashMap<>();
                            
                            for (DataSnapshot field : userSnapshot.getChildren()) {
                                userData.put(field.getKey(), field.getValue());
                            }
                            
                            topUsers.put(username, userData);
                        }
                        
                        future.complete(topUsers);
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error getting top users: " + error.getMessage());
                        future.complete(new HashMap<>());
                    }
                });
        
        return future;
    }
    

    
    /**
     * Get user streak for a specific lesson
     */
    public int getUserLessonStreak(int userId, int lessonId) {
        UserProgress progress = progressRepository.getUserLessonProgress(userId, lessonId);
        return progress != null ? progress.getStreak() : 0;
    }

    /**
     * Check if a username exists in the Firebase leaderboard
     * @param username The username to check
     * @return CompletableFuture that completes with true if username exists in leaderboard, false otherwise
     */
    public CompletableFuture<Boolean> isUsernameExistsInLeaderboard(String username) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        if (username == null || username.trim().isEmpty()) {
            Log.e(TAG, "Cannot check empty username in leaderboard");
            future.complete(false);
            return future;
        }
        
        leaderboardRef.child(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean exists = snapshot.exists();
                        Log.d(TAG, "Username '" + username + "' " + 
                              (exists ? "exists" : "does not exist") + " in leaderboard");
                        future.complete(exists);
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error checking username in leaderboard: " + error.getMessage());
                        future.complete(false);
                    }
                });
        
        return future;
    }

    /**
     * Authenticate user against Firebase database
     * @param username Username to authenticate
     * @param password Password to verify
     * @return CompletableFuture with User object if authenticated, null if not
     */
    public CompletableFuture<User> authenticateUser(String username, String password) {
        CompletableFuture<User> future = new CompletableFuture<>();
        
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            Log.e(TAG, "Cannot authenticate with empty username or password");
            future.complete(null);
            return future;
        }
        
        usersTableRef.child(username)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // User exists, verify password
                        String storedPassword = snapshot.child("password").getValue(String.class);
                        
                        // In a real app, you should use proper password hashing
                        if (storedPassword != null && storedPassword.equals(password)) {
                            // Password matches, create user object
                            Integer userId = snapshot.child("userId").getValue(Integer.class);
                            String userName = snapshot.child("userName").getValue(String.class);
                            Long userXp = snapshot.child("userXp").getValue(Long.class);
                            Long userStreak = snapshot.child("userStreak").getValue(Long.class);
                            
                            // Create user object
                            User user = new User();
                            user.setUserId(userId != null ? userId : 0);
                            user.setName(userName != null ? userName : username);
                            
                            // Thêm JoinDate để tránh lỗi NOT NULL constraint
                            user.setJoinDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
                            
                            try {
                                // Save user to local database
                                userRepository.insertUser(user);
                                
                                // Đảm bảo user đã được lưu trước khi cập nhật progress
                                AppDatabase.databaseWriteExecutor.execute(() -> {
                                    try {
                                        // Kiểm tra xem user đã được lưu thành công chưa
                                        User savedUser = userRepository.getUserById(user.getUserId());
                                        if (savedUser != null) {
                                            // Cập nhật progress sau khi user đã được lưu
                                            updateUserProgressWithFirebaseData(user.getUserId(),
                                                userXp != null ? userXp.intValue() : 0,
                                                userStreak != null ? userStreak.intValue() : 0);
                                        } else {
                                            Log.e(TAG, "User was not saved to local database");
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error checking saved user: " + e.getMessage(), e);
                                    }
                                });
                                
                                Log.d(TAG, "User authenticated successfully: " + username);
                                future.complete(user);
                            } catch (Exception e) {
                                Log.e(TAG, "Error saving user to local database: " + e.getMessage(), e);
                                future.complete(null);
                            }
                        } else {
                            Log.d(TAG, "Password mismatch for user: " + username);
                            future.complete(null);
                        }
                    } else {
                        Log.d(TAG, "User not found in Firebase: " + username);
                        future.complete(null);
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error authenticating user: " + error.getMessage());
                    future.complete(null);
                }
            });
        
        return future;
    }

    /**
     * Register a new user in Firebase
     * @param username Username to register
     * @param password Password to store
     * @param userId Local user ID
     * @return CompletableFuture with true if registered successfully
     */
    public CompletableFuture<Boolean> registerUserInFirebase(String username, String password, int userId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            Log.e(TAG, "Cannot register with empty username or password");
            future.complete(false);
            return future;
        }
        
        // Check if username already exists
        isUsernameExistsInUserTable(username)
            .thenAccept(exists -> {
                if (exists) {
                    Log.d(TAG, "Username already exists in Firebase user table: " + username);
                    future.complete(false);
                    return;
                }
                
                // Create user data map
                Map<String, Object> userData = new HashMap<>();
                userData.put("userId", userId);
                userData.put("userName", username);
                userData.put("password", password); // In a real app, hash this
                userData.put("userXp", 0);  // Default XP
                userData.put("userStreak", 1);  // Default Streak
                userData.put("isGoogleUser", false);
                
                // Save to Firebase
                usersTableRef.child(username)
                    .setValue(userData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "User registered successfully in Firebase: " + username);
                        
                        // Also update the leaderboard
                        updateLeaderboard(username, userId, 1, 0)
                            .thenAccept(success -> {
                                if (success) {
                                    Log.d(TAG, "Leaderboard updated for new user: " + username);
                                } else {
                                    Log.e(TAG, "Failed to update leaderboard for new user: " + username);
                                }
                                future.complete(true);
                            });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error registering user in Firebase: " + e.getMessage());
                        future.complete(false);
                    });
            });
        
        return future;
    }

    /**
     * Check if username exists in the user table
     */
    public CompletableFuture<Boolean> isUsernameExistsInUserTable(String username) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        if (username == null || username.trim().isEmpty()) {
            Log.e(TAG, "Cannot check empty username in user table");
            future.complete(false);
            return future;
        }
        
        usersTableRef.child(username)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean exists = snapshot.exists();
                    Log.d(TAG, "Username '" + username + "' " + 
                          (exists ? "exists" : "does not exist") + " in user table");
                    future.complete(exists);
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error checking username in user table: " + error.getMessage());
                    future.complete(false);
                }
            });
        
        return future;
    }

    /**
     * Register a Google user in Firebase
     * @param username Username (email or display name from Google)
     * @param userId Local user ID
     * @return CompletableFuture with true if registered successfully
     */
    public CompletableFuture<Boolean> registerGoogleUserInFirebase(String username, int userId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        if (username == null || username.isEmpty()) {
            Log.e(TAG, "Cannot register Google user with empty username");
            future.complete(false);
            return future;
        }
        
        // Check if username already exists
        isUsernameExistsInUserTable(username)
            .thenAccept(exists -> {
                if (exists) {
                    // User exists, pull data instead of registering
                    getUserFromFirebase(username)
                        .thenAccept(user -> {
                            if (user != null) {
                                // Update the local database with Firebase data
                                syncUserToLocal(user, null, null);
                                future.complete(true);
                            } else {
                                future.complete(false);
                            }
                        });
                    return;
                }
                
                // Create user data map for new Google user
                Map<String, Object> userData = new HashMap<>();
                userData.put("userId", userId);
                userData.put("userName", username);
                userData.put("password", ""); // Google users don't need password
                userData.put("userXp", 0);  // Default XP
                userData.put("userStreak", 1);  // Default Streak
                userData.put("isGoogleUser", true);
                
                // Save to Firebase
                usersTableRef.child(username)
                    .setValue(userData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Google user registered successfully in Firebase: " + username);
                        
                        // Also update the leaderboard
                        updateLeaderboard(username, userId, 1, 0)
                            .thenAccept(success -> {
                                future.complete(true);
                            });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error registering Google user in Firebase: " + e.getMessage());
                        future.complete(false);
                    });
            });
        
        return future;
    }

    /**
     * Get user data from Firebase
     */
    public CompletableFuture<User> getUserFromFirebase(String username) {
        CompletableFuture<User> future = new CompletableFuture<>();
        
        usersTableRef.child(username)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Integer userId = snapshot.child("userId").getValue(Integer.class);
                        String userName = snapshot.child("userName").getValue(String.class);
                        Integer userXp = snapshot.child("userXp").getValue(Integer.class);
                        Integer userStreak = snapshot.child("userStreak").getValue(Integer.class);
                        
                        User user = new User();
                        user.setUserId(userId != null ? userId : 0);
                        user.setName(userName != null ? userName : username);
                        

                        future.complete(user);
                    } else {
                        future.complete(null);
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error getting user from Firebase: " + error.getMessage());
                    future.complete(null);
                }
            });
        
        return future;
    }

    /**
     * Sync user data between Firebase and local database
     */
    private void syncUserToLocal(User user, Integer xp, Integer streak) {
        try {
            // Save or update user in local database
            userRepository.insertUser(user);
            
            // Update user progress if XP and streak are provided
            if (xp != null && streak != null) {
                List<UserProgress> progressList = progressRepository.getUserProgressList(user.getUserId());
                
                if (progressList != null && !progressList.isEmpty()) {
                    // Distribute XP among lessons
                    int xpPerLesson = xp / progressList.size();
                    int remainderXp = xp % progressList.size();
                    
                    for (int i = 0; i < progressList.size(); i++) {
                        UserProgress progress = progressList.get(i);
                        // Add extra XP to first lesson if there's a remainder
                        int lessonXp = xpPerLesson + (i == 0 ? remainderXp : 0);
                        progress.setXp(lessonXp);
                        
                        // Set streak for all lessons
                        progress.setStreak(streak);
                        
                        progressRepository.updateUserProgress(progress);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error syncing user to local database: " + e.getMessage(), e);
        }
    }

    /**
     * Update user XP and streak in Firebase
     */
    public CompletableFuture<Boolean> updateUserStats(String username, int xp, int streak) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("userXp", xp);
        updates.put("userStreak", streak);
        
        usersTableRef.child(username)
            .updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "User stats updated for " + username + ": XP=" + xp + ", Streak=" + streak);
                // Also update leaderboard
                User user = userRepository.getUserByName(username);
                if (user != null) {
                    updateLeaderboard(username, user.getUserId(), streak, xp)
                        .thenAccept(success -> future.complete(success));
                } else {
                    future.complete(true);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating user stats: " + e.getMessage());
                future.complete(false);
            });
        
        return future;
    }

    /**
     * Update user progress with data from Firebase
     */
    private void updateUserProgressWithFirebaseData(int userId, int xp, int streak) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Kiểm tra xem user có tồn tại trong database không
                User user = userRepository.getUserById(userId);
                if (user == null) {
                    Log.e(TAG, "Cannot update progress: User with ID " + userId + " not found in local database");
                    return;
                }

                // Get the UserProgressDao from the repository
                UserProgressDao progressDao = progressRepository.getUserProgressDao();
                
                // Get all progress entries for this user
                List<UserProgress> progressList = progressDao.getUserProgress(userId);
                
                if (progressList != null && !progressList.isEmpty()) {
                    // Update all progress entries with the Firebase data
                    for (UserProgress progress : progressList) {
                        progress.setXp(xp);
                        progress.setStreak(streak);
                        progressDao.update(progress);
                    }
                    
                    Log.d(TAG, "Updated " + progressList.size() + " progress entries with XP: " + xp + ", Streak: " + streak);
                } else {
                    // No progress entries found, create a default one
                    Log.d(TAG, "No progress entries found, creating default entry");
                    
                    // Lấy max progress ID để tạo ID mới
                    Integer maxProgressId = progressDao.getMaxProgressId();
                    int newProgressId = (maxProgressId != null) ? maxProgressId + 1 : 1;
                    
                    UserProgress newProgress = new UserProgress();
                    newProgress.setProgressId(newProgressId);
                    newProgress.setUserId(userId);
                    newProgress.setLessonId(1); // Sử dụng ID bài học đầu tiên
                    newProgress.setDifficultyLevel(1); // Default difficulty
                    newProgress.setCompletionTime("00:00"); // Default completion time
                    newProgress.setXp(xp);
                    newProgress.setStreak(streak);
                    newProgress.setLastStudyDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
                    
                    progressDao.insert(newProgress);
                    Log.d(TAG, "Created new progress entry with XP: " + xp + ", Streak: " + streak);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating user progress: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Adds default users to the Firebase database
     * ONLY RUN THIS FOR ONCE
     */
    public CompletableFuture<Boolean> addDefaultUsers() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        // Create a thread pool to handle parallel user creation
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        
        // Create 9 default users
        for (int i = 1; i <= 9; i++) {
            String username = "kiendeptrai" + i;
            String password = "Dinhtrungkien05@";
            
            // Generate random values
            Random random = new Random();
            int userId = random.nextInt(99999) + 1;
            int userStreak = random.nextInt(100) + 1;
            
            // Generate random XP that is divisible by 5
            int userXp = (random.nextInt(120) + 1) * 5;
            
            // Create user data for Firebase
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", userId);
            userData.put("userName", username);
            userData.put("password", password);
            userData.put("userXp", userXp);
            userData.put("userStreak", userStreak);
            userData.put("isGoogleUser", false);
            
            // Check if user already exists
            final int index = i;
            CompletableFuture<Boolean> userFuture = new CompletableFuture<>();
            futures.add(userFuture);
            
            executor.execute(() -> {
                isUsernameExistsInUserTable(username)
                    .thenAccept(exists -> {
                        if (!exists) {
                            // User doesn't exist, create it
                            usersTableRef.child(username)
                                .setValue(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Default user created: " + username);
                                    
                                    // Also update the leaderboard
                                    updateLeaderboard(username, userId, userStreak, userXp)
                                        .thenAccept(success -> {
                                            userFuture.complete(true);
                                        });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error creating default user: " + e.getMessage());
                                    userFuture.complete(false);
                                });
                        } else {
                            Log.d(TAG, "Default user already exists: " + username);
                            userFuture.complete(true);
                        }
                    });
            });
        }
        
        // Wait for all users to be created
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenRun(() -> {
                executor.shutdown();
                Log.d(TAG, "All default users created successfully");
                future.complete(true);
            })
            .exceptionally(e -> {
                executor.shutdown();
                Log.e(TAG, "Error creating default users: " + e.getMessage());
                future.complete(false);
                return null;
            });
        
        return future;
    }

    /**
     * Pulls default users from Firebase to local database
     * @return CompletableFuture that completes when all users are pulled
     */
    public CompletableFuture<Boolean> pullDefaultUsersToLocal() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        // First make sure default users exist in Firebase
        addDefaultUsers()
            .thenAccept(success -> {
                if (!success) {
                    Log.e(TAG, "Failed to create default users in Firebase");
                    future.complete(false);
                    return;
                }
                
                // Pull all default users to local database
                List<CompletableFuture<Boolean>> futures = new ArrayList<>();
                
                for (int i = 1; i <= 9; i++) {
                    String username = "kiendeptrai" + i;
                    CompletableFuture<Boolean> userFuture = new CompletableFuture<>();
                    futures.add(userFuture);
                    
                    // Get user data from Firebase
                    getUserFromFirebase(username)
                        .thenAccept(user -> {
                            if (user != null) {
                                // Save user to local database
                                userRepository.insertUser(user);
                                userFuture.complete(true);
                            } else {
                                Log.e(TAG, "Failed to pull user from Firebase: " + username);
                                userFuture.complete(false);
                            }
                        });
                }
                
                // Wait for all users to be pulled
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> {
                        Log.d(TAG, "All default users pulled to local database");
                        future.complete(true);
                    })
                    .exceptionally(e -> {
                        Log.e(TAG, "Error pulling default users: " + e.getMessage());
                        future.complete(false);
                        return null;
                    });
            });
        
        return future;
    }

    /**
     * Sync all progress data from Firebase to local database for a specific user
     * @param userId The user ID to sync progress for
     */
    public void syncAllUserProgressFromFirebase(int userId) {
        try {
            // Get user data first to verify user exists
            User user = userRepository.getUserById(userId);
            
            if (user == null) {
                Log.e(TAG, "Cannot sync progress - user " + userId + " not found in local database");
                return;
            }
            
            // Get Firebase reference for this user's progress
            DatabaseReference userProgressRef = 
                database.getReference("user_progress").child(String.valueOf(userId));
            
            // Listen for value once (not continuous)
            userProgressRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        try {
                            // Count records processed for logging
                            final int[] count = {0};
                            
                            // Process each progress record
                            for (DataSnapshot progressSnapshot : dataSnapshot.getChildren()) {
                                try {
                                    // Get progress ID
                                    Integer progressId = progressSnapshot.child("progressId").getValue(Integer.class);
                                    Integer lessonId = progressSnapshot.child("lessonId").getValue(Integer.class);
                                    Integer xp = progressSnapshot.child("xp").getValue(Integer.class);
                                    Integer streak = progressSnapshot.child("streak").getValue(Integer.class);
                                    String completionTime = progressSnapshot.child("completionTime").getValue(String.class);
                                    String lastStudyDate = progressSnapshot.child("lastStudyDate").getValue(String.class);
                                    
                                    if (progressId != null && lessonId != null) {
                                        // Check if progress exists locally
                                        UserProgress existingProgress = 
                                            progressRepository.getUserLessonProgress(userId, lessonId);
                                        
                                        if (existingProgress != null) {
                                            // Update existing progress
                                            if (xp != null) existingProgress.setXp(xp);
                                            if (streak != null) existingProgress.setStreak(streak);
                                            if (completionTime != null) existingProgress.setCompletionTime(completionTime);
                                            if (lastStudyDate != null) existingProgress.setLastStudyDate(lastStudyDate);
                                            
                                            // Save updates
                                            progressRepository.updateProgress(existingProgress);
                                        } else {
                                            // Create new progress record
                                            UserProgress newProgress = new UserProgress(
                                                progressId,
                                                userId,
                                                lessonId,
                                                0, // Default difficulty
                                                completionTime,
                                                streak != null ? streak : 1,
                                                xp != null ? xp : 0,
                                                lastStudyDate != null ? lastStudyDate : getCurrentDate()
                                            );
                                            
                                            // Save new record
                                            progressRepository.insertProgress(newProgress);
                                        }
                                        
                                        count[0]++;
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error processing progress record: " + e.getMessage(), e);
                                }
                            }
                            
                            Log.d(TAG, "Firebase sync completed: " + count[0] + " progress records updated for user " + userId);
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing Firebase progress data: " + e.getMessage(), e);
                        }
                    } else {
                        Log.d(TAG, "No progress data found in Firebase for user " + userId);
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Firebase progress sync cancelled: " + databaseError.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error syncing progress from Firebase: " + e.getMessage(), e);
        }
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }
} 