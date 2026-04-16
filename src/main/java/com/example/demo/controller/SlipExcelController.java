package com.example.demo.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.SlipExcelService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class SlipExcelController {

    private final SlipExcelService slipExcelService;

    public SlipExcelController(SlipExcelService slipExcelService) {
        this.slipExcelService = slipExcelService;
    }

    @GetMapping("/excel/export/{slipNo}")
    public void export(@PathVariable String slipNo, HttpServletResponse response) throws IOException {
        slipExcelService.exportSlip(slipNo, response);
    }

    @GetMapping("/excel/export/raw/{slipNo}")
    public void exportRaw(@PathVariable String slipNo, HttpServletResponse response) throws IOException {
        slipExcelService.exportRawSlip(slipNo, response);
    }
}
