package com.example.elsa_speak_clone;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;

import java.util.Locale;

public class SpeechToText extends AppCompatActivity {

    private static final String TAG = "SpeechToTextActivity";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    // UI elements
    private TextView tvPrompt;
    private TextView tvWord;
    private Button btnSpeak;
    private Button btnRandomWord;
    private LottieAnimationView lottieConfetti;

    // Speech recognition components
    private SpeechRecognizer speechRecognizer;
    private VoiceRecognizer voiceRecognizer;
    private boolean isRecognitionAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_speech_to_text);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeUI();
        requestMicrophonePermission();
        initializeSpeechRecognizer();
        setupVoiceRecognizer();
        setupSpeakButton();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SpeechToText.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void initializeUI() {
        tvPrompt = findViewById(R.id.tvPrompt);
        tvWord = findViewById(R.id.tvWord);
        btnRandomWord = findViewById(R.id.btnRandomWord);
        btnSpeak = findViewById(R.id.btnSpeak);
        lottieConfetti = findViewById(R.id.lottieConfetti);
    }

    private void requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    private void initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        isRecognitionAvailable = SpeechRecognizer.isRecognitionAvailable(this);
        if (!isRecognitionAvailable) {
            Log.e(TAG, "Speech recognition is not available on this device.");
            Toast.makeText(this, "Speech recognition is not available", Toast.LENGTH_LONG).show();
        }
    }

    private void setupVoiceRecognizer() {
        voiceRecognizer = new VoiceRecognizer(tvPrompt, tvWord, btnSpeak, btnRandomWord, speechRecognizer, lottieConfetti);
        voiceRecognizer.setupRandomWordButton();
        voiceRecognizer.startListening();
    }

    private void setupSpeakButton() {
        btnSpeak.setOnClickListener(v -> startSpeechRecognition());
    }

    private void startSpeechRecognition() {

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);
            try {
                speechRecognizer.startListening(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting speech recognition: ", e);
                Toast.makeText(this, "Error starting speech recognition", Toast.LENGTH_SHORT).show();
            }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Microphone permission granted.");
            } else {
                Log.w(TAG, "Microphone permission denied.");
                Toast.makeText(this, "Microphone permission is required for speech recognition.", Toast.LENGTH_LONG).show();
                // Consider disabling speech recognition features here
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}