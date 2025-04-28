package com.example.myapplication;

import java.util.HashMap;
import java.util.Map;

public class UploadItem {
    private String cr_code;
    private String cr_name;
    private String cr_category;
    private String cr_desc;
    private String cr_pdfName;
    private String cr_pdfUrl;
    private String created_at;
    private String created_by;
    private String key;
    private int likes;
    private Map<String, Boolean> liked_by; // Assuming liked_by maps user IDs to true/false

    // Default constructor (for Firebase)
    public UploadItem() {
        // Important: No arguments constructor for DataSnapshot.getValue(UploadItem.class)
    }

    // Parameterized constructor (optional, useful for manual creation)
    public UploadItem(String cr_code, String cr_name, String cr_category, String cr_desc,
                      String cr_pdfName, String cr_pdfUrl, String created_at, String created_by,String key,
                      int likes, Map<String, Boolean> liked_by) {
        this.cr_code = cr_code;
        this.cr_name = cr_name;
        this.cr_category = cr_category;
        this.cr_desc = cr_desc;
        this.cr_pdfName = cr_pdfName;
        this.cr_pdfUrl = cr_pdfUrl;
        this.created_at = created_at;
        this.created_by = created_by;
        this.key=key;
        this.likes = likes;
        this.liked_by = liked_by;
    }

    // Getters
    public String getCr_code() { return cr_code; }
    public String getCr_name() { return cr_name; }
    public String getCr_category() { return cr_category; }
    public String getCr_desc() { return cr_desc; }
    public String getCr_pdfName() { return cr_pdfName; }
    public String getCr_pdfUrl() { return cr_pdfUrl; }
    public String getCreated_at() { return created_at; }
    public String getCreated_by() { return created_by; }
    public int getLikes() { return likes; }
    public Map<String, Boolean> getLiked_by() { return liked_by; }

    // Setters (optional if you need to update values after object creation)
    public void setCr_code(String cr_code) { this.cr_code = cr_code; }
    public void setCr_name(String cr_name) { this.cr_name = cr_name; }
    public void setCr_category(String cr_category) { this.cr_category = cr_category; }
    public void setCr_desc(String cr_desc) { this.cr_desc = cr_desc; }
    public void setCr_pdfName(String cr_pdfName) { this.cr_pdfName = cr_pdfName; }
    public void setCr_pdfUrl(String cr_pdfUrl) { this.cr_pdfUrl = cr_pdfUrl; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
    public void setCreated_by(String created_by) { this.created_by = created_by; }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setLikes(int likes) { this.likes = likes; }
    public void setLiked_by(Map<String, Boolean> liked_by) { this.liked_by = liked_by; }
}
