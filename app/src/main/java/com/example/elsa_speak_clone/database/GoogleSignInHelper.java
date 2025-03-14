package com.example.elsa_speak_clone.database;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;


import com.example.elsa_speak_clone.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class GoogleSignInHelper {
    private final FirebaseAuth mAuth;
    private final GoogleSignInClient mGoogleSignInClient;
    private final Activity activity;
    private final AuthCallback authCallback;
    private static final int RC_SIGN_IN = 9001;

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(String message);
    }

    public FirebaseAuth MAuth() {
        return mAuth;
    }

    public GoogleSignInHelper(Activity activity, AuthCallback callback) {
        this.activity = activity;
        this.authCallback = callback;
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
    }
    // It will return true if have an account already signed in
    public boolean CheckGoogleLoginState() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this.activity);
        if (account != null) {
            return false;
        }
        return true;
    }
    public void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                authCallback.onError("Google sign in failed: " + e.getStatusCode());
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        authCallback.onSuccess(user);
                    } else {
                        authCallback.onError("Authentication failed: " +
                                (task.getException() != null ? task.getException().getMessage() : ""));
                    }
                });
    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(activity,
                task -> Toast.makeText(activity, "Signed out successfully", Toast.LENGTH_SHORT).show());
    }
}
