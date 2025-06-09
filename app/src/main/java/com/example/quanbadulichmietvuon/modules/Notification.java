package com.example.quanbadulichmietvuon.modules;

public class Notification {
    private String id;
    private String title;
    private String message;
    private String timestamp;

    public Notification() {
    }

    public Notification(String id, String title, String message, String timestamp) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
