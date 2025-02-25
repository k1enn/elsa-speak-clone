package com.example.elsa_speak_clone;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.elsa_speak_clone.api.FluentMeApi;
import com.example.elsa_speak_clone.api.PronunciationRequest;
import com.example.elsa_speak_clone.api.PronunciationScore;

import java.io.IOException;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import java.io.File;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class TestingActivity extends AppCompatActivity {
    private static final String BASE_URL ="https://thefluent.me/api/" ;
    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private Button btnRecord, btnStop;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        btnRecord = findViewById(R.id.btnRecord);
        btnStop = findViewById(R.id.btnStop);
        tvResult = findViewById(R.id.tvResult);

        audioFilePath = getExternalCacheDir().getAbsolutePath() + "/audio.mp3";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        FluentMeApi api = retrofit.create(FluentMeApi.class);
        btnRecord.setOnClickListener(v -> startRecording());
        btnStop.setOnClickListener(v -> stopRecordingAndSendToApi());
    }

    private void startRecording() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(audioFilePath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            btnRecord.setEnabled(false);
            btnStop.setEnabled(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecordingAndSendToApi() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
        btnRecord.setEnabled(true);
        btnStop.setEnabled(false);

        // Send the recorded audio to the API
        sendAudioToApi();
    }

    private void sendAudioToApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        FluentMeApi api = retrofit.create(FluentMeApi.class);

        // Create a file object from the recorded audio
        File audioFile = new File(audioFilePath);

        // Create a RequestBody instance from the audio file
        RequestBody requestFile = RequestBody.create(MediaType.parse("audio/mp3"), audioFile);

        // Create a MultipartBody.Part using the file request body
        MultipartBody.Part audioPart = MultipartBody.Part.createFormData("audio", audioFile.getName(), requestFile);

        // Create a request body for the text
        RequestBody textBody = RequestBody.create(MediaType.parse("text/plain"), "Hello, world!");

        Call<PronunciationScore> call = api.getPronunciationScore(textBody, audioPart);
        call.enqueue(new Callback<PronunciationScore>() {
            @Override
            public void onResponse(Call<PronunciationScore> call, Response<PronunciationScore> response) {
                if (response.isSuccessful()) {
                    PronunciationScore score = response.body();
                    tvResult.setText("Score: " + score.getScore() + "\nFeedback: " + score.getFeedback());
                } else {
                    tvResult.setText("Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PronunciationScore> call, Throwable t) {
                tvResult.setText("Network error: " + t.getMessage());
            }
        });
    }
}
