package com.example.myapplication;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class representing a course-related document uploaded by a user.
 * This includes metadata about the document and user engagement (likes).
 */
public class SourceDocumentModelClass {

    // --- Course and Document Metadata ---
    private String cr_category;     // Category of the course (e.g., Exam, Notes)
    private String cr_code;         // Course code (e.g., CS101)
    private String cr_desc;         // Description of the course material
    private String cr_name;         // Course name
    private String cr_pdfName;      // Display name of the uploaded PDF
    private String cr_pdfUrl;       // URL to access the uploaded PDF file
    private String created_at;      // Timestamp of when the document was uploaded
    private String created_by;      // Email or UID of the user who uploaded the document
    private String key;             // Unique key (used for Firebase database reference)

    // --- User Engagement Data ---
    private int likes;              // Total number of likes received
    private Map<String, Boolean> liked_by; // Map of user IDs/emails who liked the document

    /**
     * Default constructor required for Firebase deserialization.
     */
    public SourceDocumentModelClass() {}

    /**
     * Full constructor for creating a document model instance.
     */
    public SourceDocumentModelClass(String cr_category, String cr_code, String cr_desc, String cr_name,
                                    String cr_pdfName, String cr_pdfUrl, String created_at, String created_by, String key,
                                    int likes, Map<String, Boolean> liked_by) {
        this.cr_category = cr_category;
        this.cr_code = cr_code;
        this.cr_desc = cr_desc;
        this.cr_name = cr_name;
        this.cr_pdfName = cr_pdfName;
        this.cr_pdfUrl = cr_pdfUrl;
        this.created_at = created_at;
        this.created_by = created_by;
        this.key = key;
        this.likes = likes;
        this.liked_by = (liked_by != null) ? liked_by : new HashMap<>();
    }

    // --- Getters and Setters for each field ---

    public String getCr_category() {
        return cr_category;
    }

    public void setCr_category(String cr_category) {
        this.cr_category = cr_category;
    }

    public String getCr_code() {
        return cr_code;
    }

    public void setCr_code(String cr_code) {
        this.cr_code = cr_code;
    }

    public String getCr_desc() {
        return cr_desc;
    }

    public void setCr_desc(String cr_desc) {
        this.cr_desc = cr_desc;
    }

    public String getCr_name() {
        return cr_name;
    }

    public void setCr_name(String cr_name) {
        this.cr_name = cr_name;
    }

    public String getCr_pdfName() {
        return cr_pdfName;
    }

    public void setCr_pdfName(String cr_pdfName) {
        this.cr_pdfName = cr_pdfName;
    }

    /**
     * Returns the URL of the uploaded PDF file.
     * Ensures it never returns null.
     */
    public String getCr_pdfUrl() {
        return cr_pdfUrl != null ? cr_pdfUrl : "";
    }

    public void setCr_pdfUrl(String cr_pdfUrl) {
        this.cr_pdfUrl = cr_pdfUrl;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    /**
     * Returns the map of users who liked the document.
     * Initializes if null to avoid NullPointerExceptions.
     */
    public Map<String, Boolean> getLiked_by() {
        if (liked_by == null) {
            liked_by = new HashMap<>();
        }
        return liked_by;
    }

    public void setLiked_by(Map<String, Boolean> liked_by) {
        this.liked_by = liked_by;
    }
}
