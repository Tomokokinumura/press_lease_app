package com.example.demo.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.example.demo.dto.SheetSearchResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

@Service
public class GoogleSheetsService {

    private static final String APPLICATION_NAME = "Spring Boot Sheets";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final int CODE_COLUMN_INDEX = 0;
    private static final int TITLE_COLUMN_INDEX = 2;
    private static final int PRICE_COLUMN_INDEX = 8;

    private final String spreadsheetId;
    private final String range;
    private final Resource credentialsResource;
    private final String credentialsJson;
    private final String credentialsBase64;

    public GoogleSheetsService(
            @Value("${google.sheets.spreadsheet-id}") String spreadsheetId,
            @Value("${google.sheets.range}") String range,
            @Value("${google.sheets.credentials-location}") Resource credentialsResource,
            @Value("${google.sheets.credentials-json:}") String credentialsJson,
            @Value("${google.sheets.credentials-base64:}") String credentialsBase64) {
        this.spreadsheetId = spreadsheetId;
        this.range = range;
        this.credentialsResource = credentialsResource;
        this.credentialsJson = credentialsJson;
        this.credentialsBase64 = credentialsBase64;
    }

    public Optional<SheetSearchResponse> findByCode(String code) throws IOException, GeneralSecurityException {
        String normalizedCode = normalizeCode(code);
        List<List<Object>> rows = getSheetData();
        return rows.stream()
                .skip(1)
                .filter(row -> hasCell(row, CODE_COLUMN_INDEX))
                .filter(row -> normalizedCode.equals(normalizeCode(row.get(CODE_COLUMN_INDEX))))
                .findFirst()
                .map(this::toResponse);
    }

    public List<List<Object>> getSheetData() throws IOException, GeneralSecurityException {
        Sheets sheetsService = createSheetsService();
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        return response.getValues() == null ? Collections.emptyList() : response.getValues();
    }

    private Sheets createSheetsService() throws IOException, GeneralSecurityException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential;
        try (InputStream inputStream = openCredentialsStream()) {
            credential = GoogleCredential.fromStream(inputStream, httpTransport, JSON_FACTORY)
                    .createScoped(List.of(SheetsScopes.SPREADSHEETS_READONLY));
        }

        return new Sheets.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private InputStream openCredentialsStream() throws IOException {
        if (hasText(credentialsJson)) {
            return new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
        }
        if (hasText(credentialsBase64)) {
            byte[] decoded = Base64.getDecoder().decode(credentialsBase64);
            return new ByteArrayInputStream(decoded);
        }
        if (!credentialsResource.exists()) {
            throw new IOException("Google Sheets credentials were not configured.");
        }
        return credentialsResource.getInputStream();
    }

    private SheetSearchResponse toResponse(List<Object> row) {
        return new SheetSearchResponse(
                getCellValue(row, CODE_COLUMN_INDEX),
                getCellValue(row, TITLE_COLUMN_INDEX),
                getCellValue(row, PRICE_COLUMN_INDEX));
    }

    private String getCellValue(List<Object> row, int index) {
        if (index >= row.size()) {
            return "";
        }
        return String.valueOf(row.get(index));
    }

    private boolean hasCell(List<Object> row, int index) {
        return row != null && index < row.size() && row.get(index) != null;
    }

    private String normalizeCode(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Number number) {
            return new BigDecimal(number.toString())
                    .stripTrailingZeros()
                    .toPlainString()
                    .replace(".", "")
                    .trim();
        }

        String text = String.valueOf(value).trim();
        if (text.endsWith(".0")) {
            text = text.substring(0, text.length() - 2);
        }
        return text.replaceAll("\\s+", "");
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
