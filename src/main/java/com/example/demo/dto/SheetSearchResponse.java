package com.example.demo.dto;

public class SheetSearchResponse {

    private String code;
    private String name;
    private String price;

    public SheetSearchResponse() {
    }

    public SheetSearchResponse(String code, String name, String price) {
        this.code = code;
        this.name = name;
        this.price = price;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
