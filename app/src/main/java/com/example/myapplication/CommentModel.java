package com.example.myapplication;

public class CommentModel {
    public String comment;
    public String user;
    public long timestamp;

    public CommentModel() {} // Required for Firebase

    public String getComment() { return comment; }
    public String getUser() { return user; }
    public long getTimestamp() { return timestamp; }
}
