package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.SaveSlipResponse;
import com.example.demo.dto.SheetSearchResponse;
import com.example.demo.dto.SlipRequest;
import com.example.demo.service.GoogleSheetsService;
import com.example.demo.service.SlipService;

@RestController
@RequestMapping("/api")
public class SheetController {

    private final GoogleSheetsService googleSheetsService;
    private final SlipService slipService;

    public SheetController(GoogleSheetsService googleSheetsService, SlipService slipService) {
        this.googleSheetsService = googleSheetsService;
        this.slipService = slipService;
    }

    @GetMapping("/search")
    public SheetSearchResponse search(@RequestParam String code) throws Exception {
        try {
            return googleSheetsService.findByCode(code)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Code " + code + " was not found."));
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Search service is temporarily unavailable.",
                    ex);
        }
    }

    @PostMapping("/saveSlip")
    public SaveSlipResponse saveSlip(@RequestBody SlipRequest request) {
        return slipService.saveSlip(request);
    }
}