package com.example.elsa_speak_clone.classes;

public class Vocabulary {
    private int wordId;
    private String word;
    private String pronunciation;
    private int lessonId;

    public Vocabulary(int wordId, String word, String pronunciation, int lessonId) {
        this.wordId = wordId;
        this.word = word;
        this.pronunciation = pronunciation;
        this.lessonId = lessonId;
    }

    // Getters and setters
    public int getWordId() { return wordId; }
    public void setWordId(int wordId) { this.wordId = wordId; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getPronunciation() { return pronunciation; }
    public void setPronunciation(String pronunciation) { this.pronunciation = pronunciation; }

    public int getLessonId() { return lessonId; }
    public void setLessonId(int lessonId) { this.lessonId = lessonId; }
}
