package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.SlipDetailDto;
import com.example.demo.entity.SlipDetail;
import com.example.demo.mapper.SlipDetailMapper;
import com.example.demo.service.SlipService;

@RestController
@RequestMapping("/api/return")
public class ReturnApiController {

    private final SlipDetailMapper slipDetailMapper;
    private final SlipService slipService;

    public ReturnApiController(SlipDetailMapper slipDetailMapper, SlipService slipService) {
        this.slipDetailMapper = slipDetailMapper;
        this.slipService = slipService;
    }

    @GetMapping
    public List<SlipDetailDto> getSlipDetails(@RequestParam String slipNo) {
        return slipService.findBySlipNo(slipNo == null ? "" : slipNo.trim());
    }

    @PostMapping("/update")
    public void updateReturn(@RequestBody List<SlipDetail> details) {
        if (details == null) {
            return;
        }

        for (SlipDetail detail : details) {
            if (detail == null || detail.getId() == null) {
                continue;
            }

            boolean returned = Boolean.TRUE.equals(detail.getReturned());
            detail.setReturned(returned);
            detail.setReturnedDate(returned ? LocalDate.now() : null);
            slipDetailMapper.updateReturn(detail);
        }
    }
}
