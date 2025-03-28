package com.example.elsa_speak_clone.services;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.activities.ChatbotActivity;
import com.example.elsa_speak_clone.activities.DictionaryActivity;
import com.example.elsa_speak_clone.activities.LeaderboardActivity;
import com.example.elsa_speak_clone.activities.LoginActivity;
import com.example.elsa_speak_clone.activities.MainActivity;
import com.example.elsa_speak_clone.activities.NewsActivity;
import com.example.elsa_speak_clone.activities.QuizActivity;
import com.example.elsa_speak_clone.activities.RegisterActivity;
import com.example.elsa_speak_clone.activities.PronuncationActivity;
import com.example.elsa_speak_clone.fragments.HomeFragment;
import com.example.elsa_speak_clone.fragments.LearnFragment;
import com.example.elsa_speak_clone.fragments.ProfileFragment;

/**
 * Service class to handle all navigation within the app
 */
public class NavigationService {
    private static final String TAG = "NavigationService";
    private final Context context;

    public NavigationService(Context context) {
        this.context = context;
    }

    public void navigateToMain() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear top activities
        context.startActivity(intent);
    }

    public void navigateToLogin() {
        try {
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to LoginActivity", e);
        }
    }

    public void navigateToRegister() {
        try {
            Intent intent = new Intent(context, RegisterActivity.class);
            context.startActivity(intent);
            
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to RegisterActivity", e);
        }
    }

    public void navigateToSpeechToText(int lessonId) {
        try {
            Intent intent = new Intent(context, PronuncationActivity.class);
            intent.putExtra("LESSON_ID", lessonId);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to PronuncationActivity", e);
        }
    }

    public void navigateToDictionary() {
        try {
            Intent intent = new Intent(context, DictionaryActivity.class);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to Dictionary", e);
        }
    }

    public void navigateToQuiz(int lessonId) {
        try {
            Intent intent = new Intent(context, QuizActivity.class);
            intent.putExtra("LESSON_ID", lessonId);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to Quiz", e);
        }
    }

    public void loadFragment(FragmentActivity activity, Fragment fragment, boolean addToBackStack) {
        try {
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            
            if (addToBackStack) {
                transaction.addToBackStack(null);
            }
            
            transaction.commit();
        } catch (Exception e) {
            Log.e(TAG, "Error loading fragment", e);
        }
    }

    public void navigateToHomeFragment(FragmentActivity activity) {
        loadFragment(activity, new HomeFragment(), false);
    }

    public void navigateToLearnFragment(FragmentActivity activity) {
        loadFragment(activity, new LearnFragment(), false);
    }

    public void navigateToProfileFragment(FragmentActivity activity) {
        loadFragment(activity, new ProfileFragment(), false);
    }

    public void navigateToChatbot(Context context) {
        Intent intent = new Intent (context, ChatbotActivity.class);
        context.startActivity(intent);
    }

    public void navigateToLeaderboard(Context context) {
        Intent intent = new Intent (context, LeaderboardActivity.class);
        context.startActivity(intent);
    }

    public void navigateToNews(Context context) {
        Intent intent = new Intent(context, NewsActivity.class);
        context.startActivity(intent);
    }
}