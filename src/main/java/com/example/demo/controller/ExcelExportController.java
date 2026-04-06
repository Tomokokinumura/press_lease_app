package com.example.demo.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ExcelDetailRowDto;
import com.example.demo.dto.ExcelExportRequest;
import com.example.demo.entity.SlipMedia;

@RestController
@RequestMapping("/api")
public class ExcelExportController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    @PostMapping("/export")
    public ResponseEntity<byte[]> export(@RequestBody ExcelExportRequest request) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Slip");

            int rowIndex = 0;
            rowIndex = writeHeader(sheet, rowIndex, request);
            rowIndex++;
            rowIndex = writeMediaSection(sheet, rowIndex, request);
            rowIndex++;
            writeDetailSection(sheet, rowIndex, request);

            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);

            String fileName = request.getSlipNo() != null && !request.getSlipNo().isBlank()
                    ? "slip_" + request.getSlipNo() + ".xlsx"
                    : "slip_export.xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        }
    }

    private int writeHeader(XSSFSheet sheet, int rowIndex, ExcelExportRequest request) {
        Row titleRow = sheet.createRow(rowIndex++);
        titleRow.createCell(0).setCellValue("伝票情報");

        rowIndex = writeLabelValue(sheet, rowIndex, "伝票No", request.getSlipNo());
        rowIndex = writeLabelValue(sheet, rowIndex, "担当者名", request.getStaffName());
        rowIndex = writeLabelValue(sheet, rowIndex, "氏名", request.getCustomerName());
        rowIndex = writeLabelValue(sheet, rowIndex, "連絡先", request.getContactInfo());
        rowIndex = writeLabelValue(sheet, rowIndex, "メールアドレス", request.getEmailAddress());
        rowIndex = writeLabelValue(sheet, rowIndex, "貸出日", formatDate(request.getLoanDate()));
        return writeLabelValue(sheet, rowIndex, "返却日", formatDate(request.getReturnDate()));
    }

    private int writeMediaSection(XSSFSheet sheet, int rowIndex, ExcelExportRequest request) {
        Row titleRow = sheet.createRow(rowIndex++);
        titleRow.createCell(0).setCellValue("媒体情報");

        Row headerRow = sheet.createRow(rowIndex++);
        headerRow.createCell(0).setCellValue("媒体名");
        headerRow.createCell(1).setCellValue("発売日");
        headerRow.createCell(2).setCellValue("備考");

        for (SlipMedia media : request.getMediaEntries()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(nullToEmpty(media.getMediaName()));
            row.createCell(1).setCellValue(formatDate(media.getReleaseDate()));
            row.createCell(2).setCellValue(nullToEmpty(media.getNote()));
        }

        return rowIndex;
    }

    private void writeDetailSection(XSSFSheet sheet, int rowIndex, ExcelExportRequest request) {
        Row titleRow = sheet.createRow(rowIndex++);
        titleRow.createCell(0).setCellValue("明細");

        Row headerRow = sheet.createRow(rowIndex++);
        headerRow.createCell(0).setCellValue("クレジット");
        headerRow.createCell(1).setCellValue("品番");
        headerRow.createCell(2).setCellValue("タイトル");
        headerRow.createCell(3).setCellValue("価格");
        headerRow.createCell(4).setCellValue("税込価格");
        headerRow.createCell(5).setCellValue("媒体名");
        headerRow.createCell(6).setCellValue("発売日");
        headerRow.createCell(7).setCellValue("備考");

        for (ExcelDetailRowDto detail : request.getDetails()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(nullToEmpty(detail.getCredit()));
            row.createCell(1).setCellValue(nullToEmpty(detail.getCode()));
            row.createCell(2).setCellValue(nullToEmpty(detail.getName()));
            writeNumericCell(row, 3, detail.getPrice());
            writeNumericCell(row, 4, detail.getTaxPrice());
            row.createCell(5).setCellValue(nullToEmpty(detail.getMediaName()));
            row.createCell(6).setCellValue(formatDate(detail.getReleaseDate()));
            row.createCell(7).setCellValue(nullToEmpty(detail.getNote()));
        }
    }

    private int writeLabelValue(XSSFSheet sheet, int rowIndex, String label, String value) {
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(nullToEmpty(value));
        return rowIndex;
    }

    private void writeNumericCell(Row row, int index, Integer value) {
        Cell cell = row.createCell(index);
        if (value != null) {
            cell.setCellValue(value);
        } else {
            cell.setCellValue("");
        }
    }

    private String formatDate(java.time.LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
