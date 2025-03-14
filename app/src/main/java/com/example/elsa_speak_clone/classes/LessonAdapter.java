package com.example.elsa_speak_clone.classes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elsa_speak_clone.R;
import com.example.elsa_speak_clone.database.LearningAppDatabase;

import java.util.List;

// LessonAdapter.java
public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private List<Lesson> lessons;
    private LearningAppDatabase db;
    private OnLessonClickListener listener;

    public interface OnLessonClickListener {
        void onLessonClick(Lesson lesson);
    }

    public LessonAdapter(List<Lesson> lessons, LearningAppDatabase db, OnLessonClickListener listener) {
        this.lessons = lessons;
        this.db = db;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessons.get(position);

        holder.tvTitle.setText(lesson.getTopic());
        holder.tvContent.setText(lesson.getContent());

        // Retrieve vocabulary count using the corrected method
        List<String> vocabList = db.getVocabularyByLessonId(lesson.getLessonId());
        int vocabCount = (vocabList != null) ? vocabList.size() : 0;

        holder.tvVocabularyCount.setText("Vocabulary count: " + vocabCount);

        holder.itemView.setOnClickListener(v -> listener.onLessonClick(lesson));
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    static class LessonViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvVocabularyCount;

        LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvLessonTitle);
            tvContent = itemView.findViewById(R.id.tvLessonContent);
            tvVocabularyCount = itemView.findViewById(R.id.tvVocabularyCount);
        }
    }
}
