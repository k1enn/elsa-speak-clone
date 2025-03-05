package com.example.elsa_speak_clone;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LearnFragment extends Fragment {

    private TextView tvLessonTitle, tvLessonDescription, tvLessonContent;
    private LearningAppDatabase databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_learn, container, false);

        tvLessonTitle = view.findViewById(R.id.tvLessonTitle);
        tvLessonDescription = view.findViewById(R.id.tvLessonDescription);
        tvLessonContent = view.findViewById(R.id.tvLessonContent);

        databaseHelper = new LearningAppDatabase(requireContext());

        loadLessonData();

        return view;
    }

    private void loadLessonData() {
        // Assuming you have methods to get lesson data
        String lessonTitle = databaseHelper.getLessonTitle(requireContext());
        String lessonDescription = databaseHelper.getLessonDescription(requireContext());
        String lessonContent = databaseHelper.getLessonContent(requireContext());

        tvLessonTitle.setText(lessonTitle);
        tvLessonDescription.setText(lessonDescription);
        tvLessonContent.setText(lessonContent);
    }
}