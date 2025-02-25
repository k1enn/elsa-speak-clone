package com.example.elsa_speak_clone.api;

public class PronunciationScore {
    private double score;
    private String feedback;

    public double getScore() {
        return score;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
