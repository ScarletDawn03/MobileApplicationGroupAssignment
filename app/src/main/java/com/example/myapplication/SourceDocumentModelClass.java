package com.example.myapplication;

import java.util.HashMap;
import java.util.Map;

public class SourceDocumentModelClass {
    private String cr_category;
    private String cr_code;
    private String cr_desc;
    private String cr_name;
    private String cr_pdfName;
    private String cr_pdfUrl;  // This will hold the actual PDF URL
    private String created_at;
    private String created_by;
    private String key;

    private int likes;          // The number of likes for the course
    private Map<String, Boolean> liked_by; // Users who liked the course

    // Required empty constructor (for Firebase)
    public SourceDocumentModelClass() {}

    // Constructor
    public SourceDocumentModelClass(String cr_category, String cr_code, String cr_desc, String cr_name,
                                    String cr_pdfName, String cr_pdfUrl, String created_at, String created_by, String key , int likes, Map<String, Boolean> liked_by) {
        this.cr_category = cr_category;
        this.cr_code = cr_code;
        this.cr_desc = cr_desc;
        this.cr_name = cr_name;
        this.cr_pdfName = cr_pdfName;
        this.cr_pdfUrl = cr_pdfUrl;  // PDF URL set here
        this.created_at = created_at;
        this.created_by=created_by;
        this.key = key;
        this.likes = likes;
        this.liked_by = (liked_by != null) ? liked_by : new HashMap<>();
    }

    // Getters and Setters
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

    public String getCr_pdfUrl() {
        return cr_pdfUrl != null ? cr_pdfUrl : "";  // Return empty string if null
    }

    public void setCr_pdfUrl(String cr_pdfUrl) {
        this.cr_pdfUrl = cr_pdfUrl;
    }

    public String getCreated_at() {
        return created_at;
    }
    public String getCreated_by() {return created_by;}

    public void setCreated_by(String created_by) {this.created_by = created_by;}

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
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

    public Map<String, Boolean> getLiked_by() {
        return liked_by;
    }

    public void setLiked_by(Map<String, Boolean> liked_by) {
        this.liked_by = liked_by;
    }
}
