package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.SlipDetailDto;
import com.example.demo.service.SlipService;

@RestController
@RequestMapping("/api/slip")
public class SlipSearchApiController {

    private final SlipService slipService;

    public SlipSearchApiController(SlipService slipService) {
        this.slipService = slipService;
    }

    @GetMapping
    public List<SlipDetailDto> getSlip(@RequestParam String slipNo) {
        return slipService.findBySlipNo(slipNo);
    }

    @GetMapping("/code")
    public List<SlipDetailDto> searchByCode(@RequestParam String code) {
        return slipService.findByCode(code);
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
