package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TopController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/return")
    public String returnPage() {
        return "return-stage2";
    }

    @GetMapping("/slip-search")
    public String slipSearchPage() {
        return "slip-search-stage3";
    }
}
