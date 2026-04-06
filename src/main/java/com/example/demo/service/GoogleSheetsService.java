package com.example.demo.service;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    private final String spreadsheetId;
    private final String range;
    private final Resource credentialsResource;

    public GoogleSheetsService(
            @Value("${google.sheets.spreadsheet-id}") String spreadsheetId,
            @Value("${google.sheets.range}") String range,
            @Value("${google.sheets.credentials-location}") Resource credentialsResource) {
        this.spreadsheetId = spreadsheetId;
        this.range = range;
        this.credentialsResource = credentialsResource;
    }

    public Optional<SheetSearchResponse> findByCode(String code) throws IOException, GeneralSecurityException {
        List<List<Object>> rows = getSheetData();
        return rows.stream()
                .filter(row -> !row.isEmpty())
                .filter(row -> code.equals(String.valueOf(row.get(0)).trim()))
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
        if (!credentialsResource.exists()) {
            throw new IOException("Google Sheets credentials file was not found: " + credentialsResource);
        }

        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential;
        try (InputStream inputStream = credentialsResource.getInputStream()) {
            credential = GoogleCredential.fromStream(inputStream, httpTransport, JSON_FACTORY)
                    .createScoped(List.of(SheetsScopes.SPREADSHEETS_READONLY));
        }

        return new Sheets.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private SheetSearchResponse toResponse(List<Object> row) {
        return new SheetSearchResponse(
                getCellValue(row, 0),
                getCellValue(row, 1),
                getCellValue(row, 2));
    }

    private String getCellValue(List<Object> row, int index) {
        if (index >= row.size()) {
            return "";
        }
        return String.valueOf(row.get(index));
    }
}
