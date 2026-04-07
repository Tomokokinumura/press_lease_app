package com.example.demo.controller;

import java.io.IOException;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ExcelExportRequest;
import com.example.demo.service.SlipExcelService;

@RestController
@RequestMapping("/api")
public class ExcelExportController {

    private final SlipExcelService slipExcelService;

    public ExcelExportController(SlipExcelService slipExcelService) {
        this.slipExcelService = slipExcelService;
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> export(@RequestBody ExcelExportRequest request) throws IOException {
        byte[] body = slipExcelService.exportRequest(request);

        String fileName = request.getSlipNo() != null && !request.getSlipNo().isBlank()
                ? "slip_" + request.getSlipNo() + ".xlsx"
                : "slip_export.xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(body);
    }
}
