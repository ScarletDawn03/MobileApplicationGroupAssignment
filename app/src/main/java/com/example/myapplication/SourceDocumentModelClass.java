package com.example.myapplication;

public class SourceDocumentModelClass {
    private String cr_category;
    private String cr_code;
    private String cr_desc;
    private String cr_name;
    private String cr_pdfName;
    private String cr_pdfUrl;
    private String created_at;
    private String key;

    // Required empty constructor (for Firebase)
    public SourceDocumentModelClass () {}

    // Constructor
    public SourceDocumentModelClass (String cr_category, String cr_code, String cr_desc, String cr_name,
                          String cr_pdfName, String cr_pdfUrl, String created_at, String key) {
        this.cr_category = cr_category;
        this.cr_code = cr_code;
        this.cr_desc = cr_desc;
        this.cr_name = cr_name;
        this.cr_pdfName = cr_pdfName;
        this.cr_pdfUrl = cr_pdfUrl;
        this.created_at = created_at;
        this.key = key;
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
        return cr_pdfUrl;
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
