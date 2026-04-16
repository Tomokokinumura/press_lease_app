package com.example.demo.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.entity.SlipDetail;

public class ReturnUpdateRequest {

    private List<SlipDetail> details = new ArrayList<>();
    private List<Integer> deletedIds = new ArrayList<>();

    public List<SlipDetail> getDetails() {
        return details;
    }

    public void setDetails(List<SlipDetail> details) {
        this.details = details;
    }

    public List<Integer> getDeletedIds() {
        return deletedIds;
    }

    public void setDeletedIds(List<Integer> deletedIds) {
        this.deletedIds = deletedIds;
    }
}
