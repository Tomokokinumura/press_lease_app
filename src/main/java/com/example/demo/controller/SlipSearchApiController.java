package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.SlipDetailDto;
import com.example.demo.dto.SlipEditResponse;
import com.example.demo.service.SlipService;

@RestController
@RequestMapping("/api/slip")
public class SlipSearchApiController {

    private final SlipService slipService;

    public SlipSearchApiController(SlipService slipService) {
        this.slipService = slipService;
    }

    @GetMapping
    public List<SlipDetailDto> getSlip(
            @RequestParam(required = false) String slipNo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate loanDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate loanDateTo) {
        return slipService.findBySlipNo(slipNo, loanDateFrom, loanDateTo);
    }

    @GetMapping("/code")
    public List<SlipDetailDto> searchByCode(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate loanDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate loanDateTo) {
        return slipService.findByCode(code, loanDateFrom, loanDateTo);
    }

    @GetMapping("/edit")
    public SlipEditResponse getSlipForEdit(@RequestParam String slipNo) {
        return slipService.loadSlipForEdit(slipNo);
    }

    @PostMapping("/update")
    public void updateSlip(@RequestBody List<SlipDetailDto> list) {
        slipService.updateDetails(list);
    }

    @PostMapping("/delete")
    public void deleteSlip(@RequestParam String slipNo) {
        slipService.deleteBySlipNo(slipNo);
    }
}
