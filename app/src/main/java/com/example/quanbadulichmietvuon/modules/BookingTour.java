package com.example.quanbadulichmietvuon.modules;
public class BookingTour {
    private String id;
    private String userId;
    private String tourName;
    private String startDate;
    private String endDate;
    private String customerName;
    private String customerPhone;
    private int customerCount;
    private String departureLocation;
    private String createdTime;
    private boolean completed; // thêm biến này

    // Constructor mặc định (cần thiết cho Firebase)
    public BookingTour() {
    }

    // Constructor đầy đủ
    public BookingTour(String id,String userId, String tourName, String startDate, String endDate, String customerName, String customerPhone, int customerCount, String departureLocation,String createdTime,boolean completed) {
        this.id = id;
        this.userId = userId;
        this.tourName = tourName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerCount = customerCount;
        this.departureLocation = departureLocation;
        this.createdTime = createdTime;
        this.completed = completed;

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }
// Getter và Setter

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTourName() {
        return tourName;
    }

    public void setTourName(String tourName) {
        this.tourName = tourName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public int getCustomerCount() {
        return customerCount;
    }

    public void setCustomerCount(int customerCount) {
        this.customerCount = customerCount;
    }

    public String getDepartureLocation() {
        return departureLocation;
    }

    public void setDepartureLocation(String destinations) {
        this.departureLocation = departureLocation;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
