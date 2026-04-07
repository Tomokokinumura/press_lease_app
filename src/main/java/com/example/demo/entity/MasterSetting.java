package com.example.demo.entity;

import java.time.LocalDateTime;

public class MasterSetting {

    private Integer id;
    private String masterText;
    private LocalDateTime updatedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMasterText() {
        return masterText;
    }

    public void setMasterText(String masterText) {
        this.masterText = masterText;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
