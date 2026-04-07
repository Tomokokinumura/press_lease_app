package com.example.demo.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.example.demo.dto.SlipDetailDto;
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

    private final SlipMapper slipMapper;
    private final SlipDetailMapper slipDetailMapper;
    private final SlipMediaMapper slipMediaMapper;

    public SlipService(SlipMapper slipMapper, SlipDetailMapper slipDetailMapper, SlipMediaMapper slipMediaMapper) {
        this.slipMapper = slipMapper;
        this.slipDetailMapper = slipDetailMapper;
        this.slipMediaMapper = slipMediaMapper;
    }

    public String generateSlipNo() {
        String ym = LocalDate.now().format(SLIP_NO_FORMATTER);
        Integer count = slipMapper.countByMonth(ym);
        int next = (count == null ? 1 : count + 1);
        return ym + String.format("%03d", next);
    }

    @Transactional
    public SaveSlipResponse saveSlip(SlipRequest request) {
        validateRequest(request);

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

        return new SaveSlipResponse(slipNo, details.size());
    }

    public List<SlipDetailDto> findBySlipNo(String slipNo) {
        if (!hasText(slipNo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slip number is required.");
        }
        return slipDetailMapper.findSlipRowsBySlipNo(slipNo.trim());
    }

    public List<SlipDetailDto> findByCode(String code) {
        if (!hasText(code)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code is required.");
        }
        return slipDetailMapper.findSlipRowsByCode(code.trim());
    }

    @Transactional
    public void updateDetails(List<SlipDetailDto> list) {
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
}
