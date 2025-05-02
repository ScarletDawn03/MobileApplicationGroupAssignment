package com.example.myapplication;

/**
 * Model class representing a single comment entry.
 * Stores the comment text, the user who posted it, and a timestamp.
 */
public class CommentModel {

    // The text content of the comment
    private String comment;

    // The email or identifier of the user who posted the comment
    private String user;

    // The time the comment was posted, in milliseconds since epoch
    private long timestamp;

    // No-argument constructor required by Firebase
    public CommentModel() {
        // Firebase uses this constructor to create instances
    }


    /**
     * Parameterized constructor to initialize a comment model.
     *
     * @param comment   The comment text.
     * @param user      The user who made the comment.
     * @param timestamp The timestamp when the comment was posted.
     */
    public CommentModel(String comment, String user, long timestamp) {
        this.comment = comment;
        this.user = user;
        this.timestamp = timestamp;
    }

    /**
     * Gets the comment text.
     *
     * @return The comment string.
     */
    public String getComment() {
        return comment;
    }


    /**
     * Gets the user who posted the comment.
     *
     * @return The user string.
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the user who posted the comment.
     *
     * @param user The user string.
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Gets the timestamp of the comment.
     *
     * @return The timestamp in milliseconds.
     */
    public long getTimestamp() {
        return timestamp;
    }


}
