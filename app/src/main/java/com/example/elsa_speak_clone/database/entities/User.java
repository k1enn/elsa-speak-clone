package com.example.elsa_speak_clone.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Users")
public class User {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "UserId")
    private int userId;

    @ColumnInfo(name = "Gmail")
    private String gmail;

    @ColumnInfo(name = "Name")
    @NonNull
    private String name;

    @ColumnInfo(name = "Password")
    private String password;

    @ColumnInfo(name = "google")
    private boolean google;

    @ColumnInfo(name = "JoinDate", typeAffinity = ColumnInfo.TEXT)
    @NonNull
    private String joinDate;

    // Constructor
    public User(int userId, String gmail, String name, String password, boolean google, String joinDate) {
        this.userId = userId;
        this.gmail = gmail;
        this.name = name;
        this.password = password;
        this.google = google;
        this.joinDate = joinDate;
    }

    // Getters and setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getGmail() {
        return gmail;
    }

    public void setGmail(String gmail) {
        this.gmail = gmail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getGoogle() {
        return google;
    }

    public void setGoogle(boolean google) {
        this.google = google;
    }

    public String getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(String joinDate) {
        this.joinDate = joinDate;
    }

}