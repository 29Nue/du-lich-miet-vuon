package com.example.quanbadulichmietvuon.modules;

import java.util.List;
import java.io.Serializable;

public class TourBooking implements Serializable {
    private String bookingId;
    private List<String> places;
    private List<String> foods;
    private List<String> hotels;
    private int days;
    private int nights;
    private String startDate;
    private int guests;
    private String customerName;
    private String phone;
    private String email;
    private double desiredCost;
    private String notes;
    private String userId;
    private String createdTime;
    public TourBooking() {
        // constructor rỗng cần cho Firebase
    }

    public TourBooking(String bookingId, List<String> places, List<String> foods, List<String> hotels, int days, int nights, String startDate, int guests,
                       String customerName, String phone, String email, double desiredCost, String notes,String userId,String createdTime) {
        this.bookingId = bookingId;
        this.places = places;
        this.foods = foods;
        this.hotels = hotels;
        this.days = days;
        this.nights = nights;
        this.startDate = startDate;
        this.guests = guests;
        this.customerName = customerName;
        this.phone = phone;
        this.email = email;
        this.desiredCost = desiredCost;
        this.notes = notes;
        this.userId = userId;
        this.createdTime = createdTime;
    }


    // getters and setters

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public List<String> getPlaces() {
        return places;
    }

    public void setPlaces(List<String> places) {
        this.places = places;
    }

    public List<String> getFoods() {
        return foods;
    }

    public void setFoods(List<String> foods) {
        this.foods = foods;
    }

    public List<String> getHotels() {
        return hotels;
    }

    public void setHotels(List<String> hotels) {
        this.hotels = hotels;
    }

    public int getDays() { return days; }
    public void setDays(int days) { this.days = days; }

    public int getNights() { return nights; }
    public void setNights(int nights) { this.nights = nights; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public int getGuests() { return guests; }
    public void setGuests(int guests) { this.guests = guests; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getDesiredCost() { return desiredCost; }
    public void setDesiredCost(double desiredCost) { this.desiredCost = desiredCost; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}