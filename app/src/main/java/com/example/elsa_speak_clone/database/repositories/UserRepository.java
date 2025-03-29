package com.example.elsa_speak_clone.database.repositories;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.elsa_speak_clone.database.AppDatabase;
import com.example.elsa_speak_clone.database.dao.UserDao;
import com.example.elsa_speak_clone.database.entities.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private final UserDao userDao;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();

    public UserRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        this.userDao = database.userDao();
    }

    public long insertUser(User user) {
        try {
            // Insert into local database
            Future<Long> future = AppDatabase.databaseWriteExecutor.submit(() ->
                    userDao.insert(user));
            long userId = future.get();

            // Set the user ID and sync to Firebase
            if (userId > 0) {
                user.setUserId((int) userId);
                currentUser.postValue(user);
            }

            return userId;
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error inserting user", e);
            return -1;
        }
    }

    public User getUserById(int userId) {
        try {
            Future<User> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                userDao.getUserById(userId));
            User user = future.get();
            currentUser.postValue(user);
            return user;
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error getting user by ID", e);
            return null;
        }
    }

    public boolean isUsernameExists(String username) {
        try {
            Future<Integer> future = AppDatabase.databaseWriteExecutor.submit(() -> 
                userDao.checkUsernameExists(username));
            return future.get() > 0;
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error checking if username exists", e);
            return false;
        }
    }

    public User getUserByName(String username) {
        try {
            Future<User> future = AppDatabase.databaseWriteExecutor.submit(() ->
                    userDao.getUserByUsername(username));
            User user = future.get();
            if (user != null) {
                currentUser.postValue(user);
            }
            return user;
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error finding user by username", e);
            return null;
        }
    }
}