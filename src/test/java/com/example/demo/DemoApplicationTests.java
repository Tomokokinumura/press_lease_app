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
import com.example.demo.mapper.SlipDetailMapper;
import com.example.demo.mapper.SlipMapper;
import com.example.demo.mapper.SlipMediaMapper;
import com.example.demo.mapper.TodoMapper;
import com.example.demo.service.GoogleSheetsService;
import com.example.demo.service.SlipService;

@WebMvcTest({HelloController.class, SheetController.class})
class DemoApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private TodoMapper todoMapper;

	@MockBean
	private SlipMapper slipMapper;

	@MockBean
	private SlipDetailMapper slipDetailMapper;

	@MockBean
	private SlipMediaMapper slipMediaMapper;

	@MockBean
	private GoogleSheetsService googleSheetsService;

	@MockBean
	private SlipService slipService;

	@Test
	void helloPageReturnsExpectedMessage() throws Exception {
		mockMvc.perform(get("/hello"))
				.andExpect(status().isOk())
				.andExpect(view().name("hello"))
				.andExpect(model().attribute("message", "Hello! Spring Boot"))
				.andExpect(content().string(Matchers.containsString("Hello! Spring Boot")));
	}

	@Test
	void searchPageLoads() throws Exception {
		mockMvc.perform(get("/search"))
				.andExpect(status().isOk())
				.andExpect(view().name("search-ledger"))
				.andExpect(content().string(Matchers.containsString("品番検索")));
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
