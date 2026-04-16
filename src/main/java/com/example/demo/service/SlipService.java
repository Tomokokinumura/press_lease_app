package com.example.demo.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.example.demo.dto.SlipDetailDto;
import com.example.demo.dto.SlipEditResponse;
import com.example.demo.dto.SaveSlipResponse;
import com.example.demo.dto.SlipRequest;
import com.example.demo.entity.Slip;
import com.example.demo.entity.SlipDetail;
import com.example.demo.entity.SlipMedia;
import com.example.demo.mapper.SlipDetailMapper;
import com.example.demo.mapper.SlipMediaMapper;
import com.example.demo.mapper.SlipMapper;

@Service
public class SlipService {

    private static final int MAX_DETAIL_COUNT = 10;
    private static final DateTimeFormatter SLIP_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final String CREATE_SLIP_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS slip (
                id SERIAL PRIMARY KEY,
                slip_no VARCHAR(20),
                staff_name VARCHAR(50),
                customer_name VARCHAR(100),
                contact_info VARCHAR(100),
                email_address VARCHAR(100),
                loan_date DATE,
                return_date DATE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
    private static final String CREATE_SLIP_DETAIL_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS slip_detail (
                id SERIAL PRIMARY KEY,
                slip_id INT,
                code VARCHAR(20),
                name VARCHAR(100),
                price INT,
                tax_price INT,
                credit VARCHAR(100),
                planned_label VARCHAR(10),
                media_name VARCHAR(100),
                release_date DATE,
                note VARCHAR(255),
                returned BOOLEAN DEFAULT FALSE,
                returned_date DATE,
                CONSTRAINT fk_slip_detail_slip
                    FOREIGN KEY (slip_id) REFERENCES slip(id)
            )
            """;
    private static final String ALTER_SLIP_DETAIL_ADD_PLANNED_LABEL_SQL = """
            ALTER TABLE slip_detail
            ADD COLUMN IF NOT EXISTS planned_label VARCHAR(10)
            """;
    private static final String CREATE_SLIP_MEDIA_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS slip_media (
                id SERIAL PRIMARY KEY,
                slip_id INT,
                media_name VARCHAR(100),
                project_name VARCHAR(100),
                release_date DATE,
                note VARCHAR(255),
                CONSTRAINT fk_slip_media_slip
                    FOREIGN KEY (slip_id) REFERENCES slip(id)
            )
            """;

    private final SlipMapper slipMapper;
    private final SlipDetailMapper slipDetailMapper;
    private final SlipMediaMapper slipMediaMapper;
    private final JdbcTemplate jdbcTemplate;

    public SlipService(
            SlipMapper slipMapper,
            SlipDetailMapper slipDetailMapper,
            SlipMediaMapper slipMediaMapper,
            JdbcTemplate jdbcTemplate) {
        this.slipMapper = slipMapper;
        this.slipDetailMapper = slipDetailMapper;
        this.slipMediaMapper = slipMediaMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public String generateSlipNo() {
        String ym = LocalDate.now().format(SLIP_NO_FORMATTER);
        Integer count = slipMapper.countByMonth(ym);
        int next = (count == null ? 1 : count + 1);
        return ym + String.format("%03d", next);
    }

    @Transactional
    public SaveSlipResponse saveSlip(SlipRequest request) {
        ensureSlipTables();
        validateRequest(request);
        Slip slip = resolveSlipForSave(request);

        List<SlipDetail> details = request.getDetails().stream()
                .filter(this::hasMeaningfulDetail)
                .collect(Collectors.toList());

        for (SlipDetail detail : details) {
            detail.setSlipId(slip.getId());
            slipDetailMapper.insert(detail);
        }

        List<SlipMedia> mediaEntries = request.getMediaEntries().stream()
                .filter(this::hasMeaningfulMedia)
                .collect(Collectors.toList());

        for (SlipMedia media : mediaEntries) {
            media.setSlipId(slip.getId());
            slipMediaMapper.insert(media);
        }

        return new SaveSlipResponse(slip.getSlipNo(), details.size());
    }

    public List<SlipDetailDto> findBySlipNo(String slipNo) {
        if (!hasText(slipNo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slip number is required.");
        }
        return enrichWithMediaRows(slipDetailMapper.findSlipRowsBySlipNo(slipNo.trim()));
    }

    public List<SlipDetailDto> findByCode(String code) {
        if (!hasText(code)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code is required.");
        }
        return enrichWithMediaRows(slipDetailMapper.findSlipRowsByCode(code.trim()));
    }

    public SlipEditResponse loadSlipForEdit(String slipNo) {
        if (!hasText(slipNo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slip number is required.");
        }

        Slip slip = slipMapper.findBySlipNo(slipNo.trim());
        if (slip == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Slip not found: " + slipNo);
        }

        SlipEditResponse response = new SlipEditResponse();
        response.setSlipNo(slip.getSlipNo());
        response.setStaffName(slip.getStaffName());
        response.setCustomerName(slip.getCustomerName());
        response.setContactInfo(slip.getContactInfo());
        response.setEmailAddress(slip.getEmailAddress());
        response.setLoanDate(slip.getLoanDate());
        response.setReturnDate(slip.getReturnDate());
        response.setDetails(slipDetailMapper.findBySlipNo(slip.getSlipNo()));
        response.setMediaEntries(slipMediaMapper.findBySlipId(slip.getId()));
        return response;
    }

    @Transactional
    public void updateDetails(List<SlipDetailDto> list) {
        ensureSlipTables();
        if (list == null || list.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one detail row is required.");
        }
        if (list.size() > MAX_DETAIL_COUNT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can update up to 10 detail rows.");
        }

        Map<Integer, Slip> headerUpdates = new LinkedHashMap<>();

        for (SlipDetailDto dto : list) {
            if (dto == null || dto.getId() == null || dto.getSlipId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slip id and detail id are required.");
            }

            Slip slip = headerUpdates.computeIfAbsent(dto.getSlipId(), key -> {
                Slip header = new Slip();
                header.setId(dto.getSlipId());
                header.setLoanDate(dto.getLoanDate());
                header.setReturnDate(dto.getReturnDate());
                return header;
            });

            slip.setLoanDate(dto.getLoanDate());
            slip.setReturnDate(dto.getReturnDate());
            slipDetailMapper.updateDetail(dto);
        }

        for (Slip slip : headerUpdates.values()) {
            slipMapper.updateHeader(slip);
        }
    }

    @Transactional
    public void deleteBySlipNo(String slipNo) {
        ensureSlipTables();
        if (!hasText(slipNo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slip number is required.");
        }

        Integer slipId = slipMapper.findIdBySlipNo(slipNo.trim());
        if (slipId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Slip not found: " + slipNo);
        }

        slipDetailMapper.deleteBySlipId(slipId);
        slipMediaMapper.deleteBySlipId(slipId);
        slipMapper.delete(slipId);
    }

    private void validateRequest(SlipRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required.");
        }
        if (request.getLoanDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Loan date is required.");
        }
        if (request.getDetails() == null || request.getDetails().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one detail row is required.");
        }
        if (request.getDetails().size() > MAX_DETAIL_COUNT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can save up to 10 detail rows.");
        }
        if (request.getMediaEntries() != null && request.getMediaEntries().size() > MAX_DETAIL_COUNT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can save up to 10 media rows.");
        }
    }

    private boolean hasMeaningfulDetail(SlipDetail detail) {
        return detail != null && (hasText(detail.getCode())
                || hasText(detail.getName())
                || hasText(detail.getCredit())
                || hasText(detail.getMediaName())
                || hasText(detail.getNote())
                || detail.getPrice() != null
                || detail.getTaxPrice() != null
                || detail.getReleaseDate() != null);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean hasMeaningfulMedia(SlipMedia media) {
        return media != null && (hasText(media.getMediaName())
                || hasText(media.getProjectName())
                || hasText(media.getNote())
                || media.getReleaseDate() != null);
    }

    private Slip resolveSlipForSave(SlipRequest request) {
        if (hasText(request.getSlipNo())) {
            Slip existingSlip = slipMapper.findBySlipNo(request.getSlipNo().trim());
            if (existingSlip == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Slip not found: " + request.getSlipNo());
            }

            existingSlip.setStaffName(request.getStaffName());
            existingSlip.setCustomerName(request.getCustomerName());
            existingSlip.setContactInfo(request.getContactInfo());
            existingSlip.setEmailAddress(request.getEmailAddress());
            existingSlip.setLoanDate(request.getLoanDate());
            existingSlip.setReturnDate(request.getReturnDate());
            slipMapper.updateSlip(existingSlip);
            slipDetailMapper.deleteBySlipId(existingSlip.getId());
            slipMediaMapper.deleteBySlipId(existingSlip.getId());
            return existingSlip;
        }

        String slipNo = generateSlipNo();
        Slip slip = new Slip();
        slip.setSlipNo(slipNo);
        slip.setStaffName(request.getStaffName());
        slip.setCustomerName(request.getCustomerName());
        slip.setContactInfo(request.getContactInfo());
        slip.setEmailAddress(request.getEmailAddress());
        slip.setLoanDate(request.getLoanDate());
        slip.setReturnDate(request.getReturnDate());
        slipMapper.insert(slip);
        return slip;
    }

    private List<SlipDetailDto> enrichWithMediaRows(List<SlipDetailDto> rows) {
        Map<Integer, List<SlipDetailDto>> rowsBySlipId = rows.stream()
                .filter(row -> row.getSlipId() != null)
                .collect(Collectors.groupingBy(
                        SlipDetailDto::getSlipId,
                        LinkedHashMap::new,
                        Collectors.toList()));

        for (Map.Entry<Integer, List<SlipDetailDto>> entry : rowsBySlipId.entrySet()) {
            List<SlipMedia> mediaEntries = slipMediaMapper.findBySlipId(entry.getKey());
            if (mediaEntries == null) {
                mediaEntries = Collections.emptyList();
            }

            Map<String, SlipMedia> mediaByLabel = new LinkedHashMap<>();
            for (int i = 0; i < mediaEntries.size(); i++) {
                mediaByLabel.put(buildPlannedLabel(i), mediaEntries.get(i));
            }

            List<SlipDetailDto> detailRows = entry.getValue();
            for (int i = 0; i < detailRows.size(); i++) {
                SlipDetailDto row = detailRows.get(i);
                SlipMedia media = null;
                if (hasText(row.getPlannedLabel())) {
                    media = mediaByLabel.get(row.getPlannedLabel());
                }
                if (media == null && i < mediaEntries.size()) {
                    media = mediaEntries.get(i);
                }
                if (media == null) {
                    continue;
                }
                row.setMediaName(media.getMediaName());
                row.setProjectName(media.getProjectName());
                row.setReleaseDate(media.getReleaseDate());
            }
        }

        return rows;
    }

    private void ensureSlipTables() {
        jdbcTemplate.execute(CREATE_SLIP_TABLE_SQL);
        jdbcTemplate.execute(CREATE_SLIP_DETAIL_TABLE_SQL);
        jdbcTemplate.execute(ALTER_SLIP_DETAIL_ADD_PLANNED_LABEL_SQL);
        jdbcTemplate.execute(CREATE_SLIP_MEDIA_TABLE_SQL);
    }

    private String buildPlannedLabel(int index) {
        return String.valueOf((char) ('A' + index));
    }
}
