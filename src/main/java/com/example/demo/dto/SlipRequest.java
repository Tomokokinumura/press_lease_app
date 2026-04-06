package com.example.demo.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.entity.SlipDetail;
import com.example.demo.entity.SlipMedia;

public class SlipRequest {

    private String staffName;
    private LocalDate loanDate;
    private LocalDate returnDate;
    private List<SlipDetail> details = new ArrayList<>();
    private List<SlipMedia> mediaEntries = new ArrayList<>();

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public List<SlipDetail> getDetails() {
        return details;
    }

    public void setDetails(List<SlipDetail> details) {
        this.details = details;
    }

    public List<SlipMedia> getMediaEntries() {
        return mediaEntries;
    }

    public void setMediaEntries(List<SlipMedia> mediaEntries) {
        this.mediaEntries = mediaEntries;
    }
}
