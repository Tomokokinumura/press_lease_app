package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Value;

@Controller
public class HelloController {

    @Value("${supabase.url:https://jvxnujobhwjfajhrhzfh.supabase.co}")
    private String supabaseUrl;

    @Value("${supabase.anon-key:}")
    private String supabaseAnonKey;

    @GetMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("message", "Hello! Spring Boot");
        model.addAttribute("supabaseUrl", supabaseUrl);
        model.addAttribute("supabaseAnonKey", supabaseAnonKey);
        return "hello";
    }

    @GetMapping("/search")
    public String showSearchPage() {
        return "search-stage1";
    }
}
