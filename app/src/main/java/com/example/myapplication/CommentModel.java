package com.example.myapplication;

public class CommentModel {
    private String comment;
    private String user;
    private long timestamp;

    public CommentModel() {}

    public CommentModel(String comment, String user, long timestamp) {
        this.comment = comment;
        this.user = user;
        this.timestamp = timestamp;
    }

    public String getComment() { return comment; }
    public String getUser() { return user; }
    public long getTimestamp() { return timestamp; }

    public void setComment(String comment) { this.comment = comment; }
    public void setUser(String user) { this.user = user; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
