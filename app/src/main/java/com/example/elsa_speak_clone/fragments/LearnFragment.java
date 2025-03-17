package com.example.elsa_speak_clone.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elsa_speak_clone.activities.QuizActivity;
import com.example.elsa_speak_clone.classes.Lesson;
import com.example.elsa_speak_clone.classes.LessonAdapter;
import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.activities.SpeechToText;
import com.example.elsa_speak_clone.database.LearningAppDatabase;

import java.util.ArrayList;
import java.util.List;

public class LearnFragment extends Fragment {

    private final String TAG = "LearnFragment";

    private RecyclerView recyclerLessons;
    private LearningAppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_learn, container, false);

        recyclerLessons = view.findViewById(R.id.recyclerLessons);

        db = new LearningAppDatabase(requireContext());

        loadLessons();
        return view;
    }

    private void loadLessons() {

        List<Lesson> vocabList = new ArrayList<>();

        // Assuming you have lesson IDs from 1 to N (Change as needed)
        for (int i = 1; i <= 9; i++) {
            try {
                Lesson lesson = db.getLesson(requireContext(), i);
                if (lesson != null) {
                    vocabList.add(lesson);
                }
                else {
                    Log.d(TAG,"Lesson " + i + "can not found.");
                }
            } catch (Exception e) {
                Log.d(TAG, "Error in loadLesson()", e);
            } finally {
                Log.d(TAG, "Vocabulary get successful");
            }

        }

        LessonAdapter adapter = new LessonAdapter(vocabList, db, lesson -> {
            // Handle lesson click (navigate to detail fragment/activity)
            Toast.makeText(requireContext(), "Clicked " + lesson.getTopic(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireActivity(), QuizActivity.class);
            intent.putExtra("LESSON_ID", lesson.getLessonId());
            startActivity(intent);

        });

        recyclerLessons.setLayoutManager(new LinearLayoutManager(requireContext()));

        recyclerLessons.setAdapter(adapter);

    }
}