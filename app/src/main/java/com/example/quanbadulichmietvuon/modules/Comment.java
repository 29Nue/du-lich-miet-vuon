package com.example.quanbadulichmietvuon.modules;

public class Comment {
    private String commentId;
    private String userEmail;
    private String commentText;
    private long timestamp;
    private String postId;

    public Comment() {
        // required for Firebase
    }

    public Comment(String commentId, String userEmail, String commentText, long timestamp, String postId) {
        this.commentId = commentId;
        this.userEmail = userEmail;
        this.commentText = commentText;
        this.timestamp = timestamp;
        this.postId = postId;
    }

    public String getCommentId() {
        return commentId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getCommentText() {
        return commentText;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getPostId() {
        return postId;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }
}
