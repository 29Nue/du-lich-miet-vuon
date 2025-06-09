package com.example.quanbadulichmietvuon.modules;

import android.net.Uri;

public class Food {
    private String id;
    private String name;        // Tên sản phẩm
    private String description; // Mô tả sản phẩm
    private double price;       // Giá sản phẩm
    private String category;    // Danh mục sản phẩm
    private String imageUrl;    // URL của ảnh từ Firebase
    private String location;

    public Food() {
    }

    public Food(String foodId, String foodName, String description, double price, String category, String locationFood, Uri imageUri) {
    }

    // Constructor (Hàm khởi tạo)
    public Food(String id,String name, String description, double price, String category,String location, String imageUrl) {
        this.id  = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.location = location;
        this.imageUrl = imageUrl;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter và Setter cho các thuộc tính
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
