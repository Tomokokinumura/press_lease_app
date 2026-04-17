package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

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

    private String currentYm() {
        return LocalDate.now().format(SLIP_NO_FORMATTER);
    }
}
