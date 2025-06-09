package com.example.quanbadulichmietvuon.modules;

public class FavoriteItem {
    private String id;
    private String name;
    private String price;
    private String imageUrl;
    private String location; // đổi từ price thành location
    private String type; // "tour", "food", "destination", "accommodation"

    public FavoriteItem() {}

    public FavoriteItem(String id, String name, String imageUrl, String location,String price, String type) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.location = location;
        this.price = price;
        this.type = type;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getLocation() { return location; } // đổi từ getPrice() thành getLocation()
    public String getType() { return type; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPrice(String price) { this.price = price; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setLocation(String location) { this.location = location; } // đổi từ setPrice() thành setLocation()
    public void setType(String type) { this.type = type; }
}
