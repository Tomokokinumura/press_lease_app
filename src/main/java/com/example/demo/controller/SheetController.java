package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.SheetSearchResponse;
import com.example.demo.service.GoogleSheetsService;

@RestController
@RequestMapping("/api")
public class SheetController {

    private final GoogleSheetsService googleSheetsService;

    public SheetController(GoogleSheetsService googleSheetsService) {
        this.googleSheetsService = googleSheetsService;
    }

    @GetMapping("/search")
    public SheetSearchResponse search(@RequestParam String code) throws Exception {
        return googleSheetsService.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No row found for code: " + code));
    }
}
