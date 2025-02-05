package com.example.elsa_speak_clone;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.speech.RecognitionListener;
import android.os.Handler;
import android.os.Looper;

import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.Locale;

public class VoiceRecognizer {
    private final TextView tvPrompt;
    private final Button btnSpeak, btnRandomWord;
    private final SpeechRecognizer speechRecognizer;
    private final LottieAnimationView lottieConfetti;
    private String randomWord;
    private final Handler errorHandler = new Handler(Looper.getMainLooper());
    private boolean isProcessingError = false; // Prevent multiple triggers

    public VoiceRecognizer(TextView tvPrompt, Button btnSpeak, Button btnRandomWord, SpeechRecognizer speechRecognizer, LottieAnimationView lottieConfetti) {
        this.tvPrompt = tvPrompt;
        this.btnSpeak = btnSpeak;
        this.btnRandomWord = btnRandomWord;
        this.speechRecognizer = speechRecognizer;
        this.lottieConfetti = lottieConfetti;
    }

    public void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                tvPrompt.setText("Processing...");
            }

            @Override
            public void onError(int error) {
                // Delay error output until user stops speaking
                if (!isProcessingError) {
                    isProcessingError = true;
                    errorHandler.postDelayed(() -> {
                        // Remove setText when deploy
                        tvPrompt.setText("Error: " + getErrorMessage(error));
                        isProcessingError = false; // Reset flag after delay
                    }, 5000); // 5-second delay before showing error
                }
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && evaluateRecognition(matches.get(0))) {
                    showConfetti();
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        speechRecognizer.startListening(intent);
    }

    private boolean evaluateRecognition(String recognizedText) {
        if (recognizedText != null) {
            recognizedText = recognizedText.toLowerCase(Locale.US);
            if (recognizedText.equals(randomWord)) {
                tvPrompt.setText("Correct! You said: " + recognizedText);
                return true;
            } else {
                tvPrompt.setText("Incorrect! You said: " + recognizedText);
            }
        } else {
            tvPrompt.setText("No word recognized");
        }
        return false;
    }

    private String generateRandomWord() {
        String[] words = {"apple", "banana", "cat", "dog", "elephant", "fish", "giraffe", "hat", "ice", "juice",
                "kangaroo", "lion", "monkey", "notebook", "orange", "pencil", "queen", "rabbit", "snake", "tiger",
                "umbrella", "violin", "whale", "xylophone", "yellow", "zebra"};
        randomWord = words[(int) (Math.random() * words.length)];
        return randomWord;
    }

    public void setupRandomWordButton() {
        btnRandomWord.setOnClickListener(v -> {
            tvPrompt.setText("Say this word: " + generateRandomWord());
        });
    }

    private void showConfetti() {
        lottieConfetti.setAnimation(R.raw.confetti);
        lottieConfetti.setVisibility(View.VISIBLE);
        lottieConfetti.playAnimation();
        lottieConfetti.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}

            @Override
            public void onAnimationEnd(Animator animator) {
                lottieConfetti.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });
    }

    private String getErrorMessage(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO: return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT: return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK: return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH: return "No speech input";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "Speech recognizer is busy";
            case SpeechRecognizer.ERROR_SERVER: return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return "Speech timeout";
            default: return "Unknown error";
        }
    }

    public void release() {
        speechRecognizer.destroy();
    }
}
