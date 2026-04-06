package com.example.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.controller.HelloController;
import com.example.demo.controller.SheetController;
import com.example.demo.dto.SheetSearchResponse;
import com.example.demo.mapper.TodoMapper;
import com.example.demo.service.GoogleSheetsService;

@WebMvcTest({HelloController.class, SheetController.class})
class DemoApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private TodoMapper todoMapper;

	@MockBean
	private GoogleSheetsService googleSheetsService;

	@Test
	void helloPageReturnsExpectedMessage() throws Exception {
		mockMvc.perform(get("/hello"))
				.andExpect(status().isOk())
				.andExpect(view().name("hello"))
				.andExpect(model().attribute("message", "Hello! Spring Boot"))
				.andExpect(content().string(Matchers.containsString("Hello! Spring Boot")));
	}

	@Test
	void searchReturnsMatchingSheetRow() throws Exception {
		given(googleSheetsService.findByCode("A001"))
				.willReturn(Optional.of(new SheetSearchResponse("A001", "商品名", "1000")));

		mockMvc.perform(get("/api/search").param("code", "A001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("A001"))
				.andExpect(jsonPath("$.name").value("商品名"))
				.andExpect(jsonPath("$.price").value("1000"));
	}

}
