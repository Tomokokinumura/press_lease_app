package com.example.demo.controller;

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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
    public void exportRaw(
            @PathVariable String slipNo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate loanDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate loanDateTo,
            HttpServletResponse response) throws IOException {
        slipExcelService.exportRawSlip(slipNo, loanDateFrom, loanDateTo, response);
    }

    @GetMapping("/excel/export/raw/code/{code}")
    public void exportRawByCode(
            @PathVariable String code,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate loanDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate loanDateTo,
            HttpServletResponse response) throws IOException {
        slipExcelService.exportRawByCode(code, loanDateFrom, loanDateTo, response);
    }
}
