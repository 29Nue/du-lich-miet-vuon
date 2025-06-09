package com.example.quanbadulichmietvuon.modules;

import java.util.List;

public class JourneyPost {
    private String postId; // thêm id
    private String userEmail;
    private String postTime;
    private String description;
    private List<String> journeyImages;
    private int likeCount;
    private int commentCount;
    private String visibility;
    public JourneyPost() {
        // constructor mặc định để dùng với Firebase
    }

    public JourneyPost(String postId, String userEmail, String postTime, String description,
                       List<String> journeyImages, int likeCount, int commentCount,String visibility) {
        this.postId = postId;
        this.userEmail = userEmail;
        this.postTime = postTime;
        this.description = description;
        this.journeyImages = journeyImages;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.visibility = visibility;
    }

    // getter và setter
    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getJourneyImages() {
        return journeyImages;
    }

    public void setJourneyImages(List<String> journeyImages) {
        this.journeyImages = journeyImages;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
}
