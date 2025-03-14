package com.example.elsa_speak_clone.classes;

public class Lesson {
    private int lessonId;
    private String topic;
    private String content;
    private int difficultyLevel;
    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setDifficultyLevel(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    // Getters
    public int getLessonId() { return lessonId; }
    public String getTopic() { return topic; }
    public String getContent() { return content; }
    public int getDifficultyLevel() { return difficultyLevel; }

    public Lesson(int lessonId, String topic, String content, int difficultyLevel) {
        this.lessonId = lessonId;
        this.topic = topic;
        this.content = content;
        this.difficultyLevel = difficultyLevel;
    }


}
