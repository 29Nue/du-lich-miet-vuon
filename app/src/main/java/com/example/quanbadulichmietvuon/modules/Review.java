package com.example.quanbadulichmietvuon.modules;

import java.io.Serializable;

public class Review implements Serializable {

    private String id;
    private int rating;
    private String reason;
    private String userName;
    private long timestamp;

    public Review() {} // constructor rỗng cho firebase

    public Review(String id, int rating, String reason, String userName, long timestamp) {
        this.id = id;
        this.rating = rating;
        this.reason = reason;
        this.userName = userName;
        this.timestamp = timestamp;
    }

    // getter và setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }


    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
