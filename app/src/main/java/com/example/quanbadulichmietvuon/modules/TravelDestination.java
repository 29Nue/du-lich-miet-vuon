package com.example.quanbadulichmietvuon.modules;

import java.util.List;
import java.io.Serializable;

public class TravelDestination implements Serializable {

    public enum TravelDestinationType {
        VUON_TRAI_CAY, DIEM_THAM_QUAN, KHU_DU_LICH
    }

    private String id, name, description, address;
    private List<String> images;
    private boolean isActive;
    private double rating;
    private int reviewCount;
    private TravelDestinationType type;

    public TravelDestination() {
    }

    public TravelDestination(String id, String name, List<String> images, boolean isActive, String description, String address, double rating, int reviewCount, TravelDestinationType type) {
        this.id = id;
        this.name = name;
        this.images = images;
        this.isActive = isActive;
        this.description = description;
        this.address = address;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.type = type;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public TravelDestinationType getType() { return type; }
    public void setType(TravelDestinationType type) { this.type = type; }
}
