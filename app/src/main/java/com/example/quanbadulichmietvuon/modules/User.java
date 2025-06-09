package com.example.quanbadulichmietvuon.modules;


public class User {
    private String id;
    private String email;
    private String username;
    private String phone;
    private String photo;

    public User(String id, String email, String username, String phone, String photo) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.phone = phone;
        this.photo = photo;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPhone() {
        return phone;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhoto() {
        return photo;
    }
}
