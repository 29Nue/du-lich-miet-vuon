package com.example.quanbadulichmietvuon.modules;

import java.util.List;

public class Accommodation {
    private String id;                   // Unique ID for the accommodation
    private List<String> imageResId;           // Resource ID for the image
    private String name;                 // Name of the accommodation
    private String location;             // Location of the accommodation
    private String rating;               // Rating of the accommodation
    private AccommodationType type;   // Type of accommodation: HOTEL, HOMESTAY, ECOLODGE, GUESTHOUSE
    private double price;
    private String description;


    // Enum to represent accommodation type
    public enum AccommodationType {
        HOTEL,
        HOMESTAY,
        ECOLODGE,
        GUESTHOUSE
    }

    public Accommodation() {
    }

    // Constructor with id
    public Accommodation(String id, List<String> imageResId, String name, String location, String rating, AccommodationType type,double price, String description) {
        this.id = id;
        this.imageResId = imageResId;
        this.name = name;
        this.location = location;
        this.rating = rating;
        this.type = type;
        this.description = description;
        this.price = price;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getImageResId() {
        return imageResId;
    }

    public void setImageResId(List<String> imageResId) {
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public AccommodationType getType() {
        return type;
    }

    public void setType(AccommodationType type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
