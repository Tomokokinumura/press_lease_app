package com.example.demo.dto;

public class SaveSlipResponse {

    private String slipNo;
    private int detailCount;

    public SaveSlipResponse(String slipNo, int detailCount) {
        this.slipNo = slipNo;
        this.detailCount = detailCount;
    }

    public String getSlipNo() {
        return slipNo;
    }

    public int getDetailCount() {
        return detailCount;
    }
}
