package com.example.demo.entity;

import java.time.LocalDate;

public class SlipMedia {

    private Integer id;
    private Integer slipId;
    private String mediaName;
    private LocalDate releaseDate;
    private String note;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSlipId() {
        return slipId;
    }

    public void setSlipId(Integer slipId) {
        this.slipId = slipId;
    }

    public String getMediaName() {
        return mediaName;
    }

    public void setMediaName(String mediaName) {
        this.mediaName = mediaName;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
