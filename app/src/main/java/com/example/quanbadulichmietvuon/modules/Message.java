package com.example.quanbadulichmietvuon.modules;
public class Message {
    private String sender;
    private String message;
    private long timestamp;

    // constructor rỗng cho Firebase
    public Message() {
    }

    public Message(String sender, String message, long timestamp) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

