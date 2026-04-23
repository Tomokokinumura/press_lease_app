package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.SlipDetailDto;
import com.example.demo.mapper.SlipDetailMapper;
import com.example.demo.mapper.SlipMapper;
import com.example.demo.mapper.SlipMediaMapper;

class SlipServiceTests {

    private static final DateTimeFormatter SLIP_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    @Test
    void generateSlipNoStartsFrom001WhenMonthHasNoSlips() {
        SlipMapper slipMapper = mock(SlipMapper.class);
        when(slipMapper.findLatestSlipNoByMonth(currentYm())).thenReturn(null);

        SlipService service = new SlipService(
                slipMapper,
                mock(SlipDetailMapper.class),
                mock(SlipMediaMapper.class),
                mock(JdbcTemplate.class));

        assertEquals(currentYm() + "001", service.generateSlipNo());
    }

    @Test
    void generateSlipNoIncrementsFromLatestSlipNoWithinMonth() {
        SlipMapper slipMapper = mock(SlipMapper.class);
        when(slipMapper.findLatestSlipNoByMonth(currentYm())).thenReturn(currentYm() + "017");

        SlipService service = new SlipService(
                slipMapper,
                mock(SlipDetailMapper.class),
                mock(SlipMediaMapper.class),
                mock(JdbcTemplate.class));

        assertEquals(currentYm() + "018", service.generateSlipNo());
    }

    @Test
    void generateSlipNoFailsWhenMonthlySequenceIsExhausted() {
        SlipMapper slipMapper = mock(SlipMapper.class);
        when(slipMapper.findLatestSlipNoByMonth(currentYm())).thenReturn(currentYm() + "999");

        SlipService service = new SlipService(
                slipMapper,
                mock(SlipDetailMapper.class),
                mock(SlipMediaMapper.class),
                mock(JdbcTemplate.class));

        ResponseStatusException error = assertThrows(ResponseStatusException.class, service::generateSlipNo);
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertTrue(error.getReason().contains(currentYm()));
    }

    @Test
    void findBySlipNoAllowsDateOnlySearch() {
        SlipDetailMapper slipDetailMapper = mock(SlipDetailMapper.class);
        SlipDetailDto row = new SlipDetailDto();
        row.setSlipId(1);
        when(slipDetailMapper.findSlipRowsBySlipNoWithLoanDateRange(
                null,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30)))
                .thenReturn(List.of(row));

        SlipService service = new SlipService(
                mock(SlipMapper.class),
                slipDetailMapper,
                mock(SlipMediaMapper.class),
                mock(JdbcTemplate.class));

        List<SlipDetailDto> result = service.findBySlipNo(null, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        assertEquals(1, result.size());
        verify(slipDetailMapper).findSlipRowsBySlipNoWithLoanDateRange(
                null,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30));
    }

    @Test
    void findBySlipNoRejectsSearchWithoutTextAndDateRange() {
        SlipService service = new SlipService(
                mock(SlipMapper.class),
                mock(SlipDetailMapper.class),
                mock(SlipMediaMapper.class),
                mock(JdbcTemplate.class));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.findBySlipNo(null, null, null));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        assertEquals("Slip number or loan date range is required.", error.getReason());
    }

    @Test
    void findByCodeAllowsDateOnlySearch() {
        SlipDetailMapper slipDetailMapper = mock(SlipDetailMapper.class);
        SlipDetailDto row = new SlipDetailDto();
        row.setSlipId(1);
        when(slipDetailMapper.findSlipRowsByCodeWithLoanDateRange(
                null,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30)))
                .thenReturn(List.of(row));

        SlipService service = new SlipService(
                mock(SlipMapper.class),
                slipDetailMapper,
                mock(SlipMediaMapper.class),
                mock(JdbcTemplate.class));

        List<SlipDetailDto> result = service.findByCode(null, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        assertEquals(1, result.size());
        verify(slipDetailMapper).findSlipRowsByCodeWithLoanDateRange(
                null,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30));
    }

    @Test
    void findByCodeRejectsSearchWithoutTextAndDateRange() {
        SlipService service = new SlipService(
                mock(SlipMapper.class),
                mock(SlipDetailMapper.class),
                mock(SlipMediaMapper.class),
                mock(JdbcTemplate.class));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.findByCode(null, null, null));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        assertEquals("Code or loan date range is required.", error.getReason());
    }

    private String currentYm() {
        return LocalDate.now().format(SLIP_NO_FORMATTER);
    }
}
