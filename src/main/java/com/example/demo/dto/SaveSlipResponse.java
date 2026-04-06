package com.example.demo.dto;

public class SaveSlipResponse {

    private boolean success;
    private String slipNo;
    private int detailCount;

    public SaveSlipResponse(String slipNo, int detailCount) {
        this.success = true;
        this.slipNo = slipNo;
        this.detailCount = detailCount;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getSlipNo() {
        return slipNo;
    }

    public int getDetailCount() {
        return detailCount;
    }
}
