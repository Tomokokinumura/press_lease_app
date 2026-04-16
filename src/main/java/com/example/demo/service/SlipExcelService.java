package com.example.demo.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.ExcelDetailRowDto;
import com.example.demo.dto.ExcelExportRequest;
import com.example.demo.dto.SlipDetailDto;
import com.example.demo.entity.MasterSetting;
import com.example.demo.entity.Slip;
import com.example.demo.entity.SlipDetail;
import com.example.demo.entity.SlipMedia;
import com.example.demo.mapper.MasterSettingMapper;
import com.example.demo.mapper.SlipDetailMapper;
import com.example.demo.mapper.SlipMapper;
import com.example.demo.mapper.SlipMediaMapper;

import jakarta.servlet.http.HttpServletResponse;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class SlipExcelService {

    private static final String TEMPLATE_PATH = "/template/sanpleformat.xlsx";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final int MAX_MEDIA_ROWS = 10;
    private static final int MAX_DETAIL_ROWS = 25;

    private final SlipMapper slipMapper;
    private final SlipDetailMapper slipDetailMapper;
    private final SlipMediaMapper slipMediaMapper;
    private final MasterSettingMapper masterSettingMapper;

    public SlipExcelService(
            SlipMapper slipMapper,
            SlipDetailMapper slipDetailMapper,
            SlipMediaMapper slipMediaMapper,
            MasterSettingMapper masterSettingMapper) {
        this.slipMapper = slipMapper;
        this.slipDetailMapper = slipDetailMapper;
        this.slipMediaMapper = slipMediaMapper;
        this.masterSettingMapper = masterSettingMapper;
    }

    public void exportSlip(String slipNo, HttpServletResponse response) throws IOException {
        Slip slip = slipMapper.findBySlipNo(slipNo);
        if (slip == null) {
            throw new ResponseStatusException(NOT_FOUND, "Slip not found: " + slipNo);
        }

        List<SlipDetail> details = slipDetailMapper.findBySlipNo(slipNo);
        List<SlipMedia> mediaEntries = slip.getId() != null
                ? slipMediaMapper.findBySlipId(slip.getId())
                : Collections.emptyList();
        MasterSetting masterSetting = masterSettingMapper.find();

        byte[] bytes = renderWorkbook(
                slip.getSlipNo(),
                slip.getStaffName(),
                slip.getCustomerName(),
                slip.getContactInfo(),
                slip.getEmailAddress(),
                slip.getLoanDate(),
                slip.getReturnDate(),
                mediaEntries,
                details.stream().map(this::toExcelDetailRow).toList(),
                masterSetting != null ? masterSetting.getMasterText() : "");

        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=slip_" + slipNo + ".xlsx");
        response.getOutputStream().write(bytes);
        response.flushBuffer();
    }

    public void exportRawSlip(String slipNo, HttpServletResponse response) throws IOException {
        Slip slip = slipMapper.findBySlipNo(slipNo);
        if (slip == null) {
            throw new ResponseStatusException(NOT_FOUND, "Slip not found: " + slipNo);
        }

        List<SlipDetailDto> rows = enrichRowsWithMedia(slipDetailMapper.findSlipRowsBySlipNo(slipNo));

        byte[] bytes = renderRawWorkbook(rows);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=slip_raw_" + slipNo + ".xlsx");
        response.getOutputStream().write(bytes);
        response.flushBuffer();
    }

    public void exportRawByCode(String code, HttpServletResponse response) throws IOException {
        List<SlipDetailDto> rows = enrichRowsWithMedia(slipDetailMapper.findSlipRowsByCode(code));
        if (rows.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "No rows found for code: " + code);
        }

        byte[] bytes = renderRawWorkbook(rows);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=slip_raw_code_" + code + ".xlsx");
        response.getOutputStream().write(bytes);
        response.flushBuffer();
    }

    public byte[] exportRequest(ExcelExportRequest request) throws IOException {
        MasterSetting masterSetting = masterSettingMapper.find();
        return renderWorkbook(
                request.getSlipNo(),
                request.getStaffName(),
                request.getCustomerName(),
                request.getContactInfo(),
                request.getEmailAddress(),
                request.getLoanDate(),
                request.getReturnDate(),
                request.getMediaEntries(),
                request.getDetails(),
                masterSetting != null ? masterSetting.getMasterText() : "");
    }

    private byte[] renderWorkbook(
            String slipNo,
            String staffName,
            String customerName,
            String contactInfo,
            String emailAddress,
            LocalDate loanDate,
            LocalDate returnDate,
            List<SlipMedia> mediaEntries,
            List<ExcelDetailRowDto> details,
            String masterText) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(TEMPLATE_PATH)) {
            if (inputStream == null) {
                throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Excel template not found.");
            }

            try (Workbook workbook = new XSSFWorkbook(inputStream);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.getSheetAt(0);

                writeHeader(sheet, slipNo, staffName, customerName, contactInfo, emailAddress, loanDate, returnDate);
                writeMediaEntries(sheet, mediaEntries);
                writeDetails(sheet, details);
                writeMasterText(sheet, masterText);

                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        }
    }

    private byte[] renderRawWorkbook(List<SlipDetailDto> rows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("slip-raw");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            String[] headers = {
                    "Status", "Slip No", "Staff Name", "Code", "Title", "Price", "Tax Price",
                    "Credit", "Media Name", "Project Name", "Release Date", "Loan Date", "Return Date", "Note"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < rows.size(); i++) {
                SlipDetailDto rowData = rows.get(i);
                Row row = sheet.createRow(i + 1);
                setTextCell(row, 0, Boolean.TRUE.equals(rowData.getReturned()) ? "Returned" : "");
                setTextCell(row, 1, rowData.getSlipNo());
                setTextCell(row, 2, rowData.getStaffName());
                setTextCell(row, 3, rowData.getCode());
                setTextCell(row, 4, rowData.getName());
                setNumberCell(row, 5, rowData.getPrice());
                setNumberCell(row, 6, rowData.getTaxPrice());
                setTextCell(row, 7, rowData.getCredit());
                setTextCell(row, 8, rowData.getMediaName());
                setTextCell(row, 9, rowData.getProjectName());
                setDateCell(row, 10, rowData.getReleaseDate(), dateStyle);
                setDateCell(row, 11, rowData.getLoanDate(), dateStyle);
                setDateCell(row, 12, rowData.getReturnDate(), dateStyle);
                setTextCell(row, 13, rowData.getNote());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void writeHeader(
            Sheet sheet,
            String slipNo,
            String staffName,
            String customerName,
            String contactInfo,
            String emailAddress,
            LocalDate loanDate,
            LocalDate returnDate) {
        setText(sheet, 1, 3, slipNo);
        setText(sheet, 1, 14, formatDate(loanDate));
        setText(sheet, 2, 3, customerName);
        setText(sheet, 2, 14, formatDate(returnDate));
        setText(sheet, 3, 3, contactInfo);
        setText(sheet, 3, 14, staffName);
        setText(sheet, 4, 3, emailAddress);
    }

    private void writeMediaEntries(Sheet sheet, List<SlipMedia> mediaEntries) {
        int rowCount = Math.min(mediaEntries != null ? mediaEntries.size() : 0, MAX_MEDIA_ROWS);
        for (int i = 0; i < rowCount; i++) {
            SlipMedia media = mediaEntries.get(i);
            int rowIndex = 7 + i;
            setNumber(sheet, rowIndex, 1, i + 1);
            setText(sheet, rowIndex, 2, media.getMediaName());
            setText(sheet, rowIndex, 5, formatDate(media.getReleaseDate()));
            setText(sheet, rowIndex, 7, media.getProjectName());
            setText(sheet, rowIndex, 15, media.getNote());
        }
    }

    private void writeDetails(Sheet sheet, List<ExcelDetailRowDto> details) {
        int rowCount = Math.min(details != null ? details.size() : 0, MAX_DETAIL_ROWS);
        for (int i = 0; i < rowCount; i++) {
            ExcelDetailRowDto detail = details.get(i);
            int rowIndex = 19 + i;
            setNumber(sheet, rowIndex, 1, i + 1);
            setText(sheet, rowIndex, 2, detail.getCode());
            setText(sheet, rowIndex, 4, detail.getName());
            setNumber(sheet, rowIndex, 7, detail.getPrice());
            setNumber(sheet, rowIndex, 9, detail.getTaxPrice());
            setText(sheet, rowIndex, 11, detail.getCredit());
            setText(sheet, rowIndex, 14, detail.getNote());
        }
    }

    private void writeMasterText(Sheet sheet, String masterText) {
        setText(sheet, 45, 1, masterText);
    }

    private List<SlipDetailDto> enrichRowsWithMedia(List<SlipDetailDto> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        Integer currentSlipId = null;
        List<SlipMedia> mediaEntries = Collections.emptyList();
        int mediaIndex = 0;

        for (SlipDetailDto row : rows) {
            if (row.getSlipId() == null) {
                continue;
            }
            if (!row.getSlipId().equals(currentSlipId)) {
                currentSlipId = row.getSlipId();
                mediaEntries = slipMediaMapper.findBySlipId(currentSlipId);
                if (mediaEntries == null) {
                    mediaEntries = Collections.emptyList();
                }
                mediaIndex = 0;
            }
            if (mediaIndex >= mediaEntries.size()) {
                continue;
            }
            SlipMedia media = mediaEntries.get(mediaIndex++);
            row.setMediaName(media.getMediaName());
            row.setProjectName(media.getProjectName());
            row.setReleaseDate(media.getReleaseDate());
        }
        return rows;
    }

    private ExcelDetailRowDto toExcelDetailRow(SlipDetail detail) {
        ExcelDetailRowDto row = new ExcelDetailRowDto();
        row.setCredit(detail.getCredit());
        row.setCode(detail.getCode());
        row.setName(detail.getName());
        row.setPrice(detail.getPrice());
        row.setTaxPrice(detail.getTaxPrice());
        row.setMediaName(detail.getMediaName());
        row.setReleaseDate(detail.getReleaseDate());
        row.setNote(detail.getNote());
        return row;
    }

    private void setText(Sheet sheet, int rowIndex, int cellIndex, String value) {
        Cell cell = getOrCreateCell(sheet, rowIndex, cellIndex);
        cell.setCellValue(value == null ? "" : value);
    }

    private void setNumber(Sheet sheet, int rowIndex, int cellIndex, Integer value) {
        Cell cell = getOrCreateCell(sheet, rowIndex, cellIndex);
        if (value == null) {
            cell.setBlank();
            return;
        }
        cell.setCellValue(value);
    }

    private void setNumber(Sheet sheet, int rowIndex, int cellIndex, int value) {
        Cell cell = getOrCreateCell(sheet, rowIndex, cellIndex);
        cell.setCellValue(value);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CreationHelper creationHelper = workbook.getCreationHelper();
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy/mm/dd"));
        return style;
    }

    private void setTextCell(Row row, int cellIndex, String value) {
        Cell cell = row.createCell(cellIndex);
        cell.setCellValue(value == null ? "" : value);
    }

    private void setNumberCell(Row row, int cellIndex, Integer value) {
        Cell cell = row.createCell(cellIndex);
        if (value == null) {
            cell.setBlank();
        } else {
            cell.setCellValue(value);
        }
    }

    private void setDateCell(Row row, int cellIndex, LocalDate value, CellStyle dateStyle) {
        Cell cell = row.createCell(cellIndex);
        if (value == null) {
            cell.setBlank();
            return;
        }
        cell.setCellValue(java.sql.Date.valueOf(value));
        cell.setCellStyle(dateStyle);
    }

    private Cell getOrCreateCell(Sheet sheet, int rowIndex, int cellIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            cell = row.createCell(cellIndex);
        }
        return cell;
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }
}
