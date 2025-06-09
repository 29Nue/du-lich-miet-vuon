package com.example.quanbadulichmietvuon.modules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Tour implements Serializable {
    private String id; // ID của tour
    private String tourName; // Tên tour
    private String shortDescription; // Mô tả ngắn
    private String durationDays; // Số ngày
    private String durationNights; // Số đêm
    private String startDate; // Ngày khởi hành
    private String endDate; // Ngày kết thúc
    private String destinations; // Danh sách địa điểm
    private String itinerary; // Lịch trình
    private String price; // Giá tour
    private String includedServices; // Dịch vụ bao gồm
    private String accommodation; // Thông tin chỗ ở
    private String tourGuide; // Thông tin hướng dẫn viên
    private String cancellationPolicy; // Chính sách hủy
    private String photo;// Hình ảnh

    public Tour() {
    }

    // Constructor
    public Tour(String id, String tourName, String shortDescription, String durationDays,
                String durationNights, String startDate, String endDate,
                String destinations, String itinerary, String price,
                String includedServices, String accommodation,
                String tourGuide, String cancellationPolicy,
                String photo) {
        this.id = id;
        this.tourName = tourName;
        this.shortDescription = shortDescription;
        this.durationDays = durationDays;
        this.durationNights = durationNights;
        this.startDate = startDate;
        this.endDate = endDate;
        this.destinations = destinations;
        this.itinerary = itinerary;
        this.price = price;
        this.includedServices = includedServices;
        this.accommodation = accommodation;
        this.tourGuide = tourGuide;
        this.cancellationPolicy = cancellationPolicy;
        this.photo = photo;
       // this.reviews = new ArrayList<>(); // Khởi tạo danh sách đánh giá
    }

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

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(String durationDays) {
        this.durationDays = durationDays;
    }

    public String getDurationNights() {
        return durationNights;
    }

    public void setDurationNights(String durationNights) {
        this.durationNights = durationNights;
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

    public String getDestinations() {
        return destinations;
    }

    public void setDestinations(String destinations) {
        this.destinations = destinations;
    }

    public String getItinerary() {
        return itinerary;
    }

    public void setItinerary(String itinerary) {
        this.itinerary = itinerary;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getIncludedServices() {
        return includedServices;
    }

    public void setIncludedServices(String includedServices) {
        this.includedServices = includedServices;
    }

    public String getAccommodation() {
        return accommodation;
    }

    public void setAccommodation(String accommodation) {
        this.accommodation = accommodation;
    }

    public String getTourGuide() {
        return tourGuide;
    }

    public void setTourGuide(String tourGuide) {
        this.tourGuide = tourGuide;
    }

    public String getCancellationPolicy() {
        return cancellationPolicy;
    }

    public void setCancellationPolicy(String cancellationPolicy) {
        this.cancellationPolicy = cancellationPolicy;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
    // Constructor chỉ với các trường cần thiết cho adapter
    public Tour(String tourName, String startDate, String endDate, String price, String photo) {
        this.tourName = tourName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.price = price;
        this.photo = photo;
    }

}


