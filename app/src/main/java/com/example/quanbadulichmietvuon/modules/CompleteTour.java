package com.example.quanbadulichmietvuon.modules;

import java.io.Serializable;

public class CompleteTour implements Serializable {
    private String tourId;
    private String tourName;
    private String startDate;
    private String endDate;
    private String completeTime;

    // Constructor mặc định (yêu cầu khi sử dụng với Firebase)
    public CompleteTour() {
    }

    // Constructor đầy đủ
    public CompleteTour(String tourId, String tourName, String startDate, String endDate,String completeTime) {
        this.tourId = tourId;
        this.tourName = tourName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.completeTime = completeTime;
    }

    // Getter và Setter
    public String getTourId() {
        return tourId;
    }

    public void setTourId(String tourId) {
        this.tourId = tourId;
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

    public String getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(String completeTime) {
        this.completeTime = completeTime;
    }
}
