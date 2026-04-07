package com.example.demo;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.controller.ExcelExportController;
import com.example.demo.controller.HelloController;
import com.example.demo.controller.ReturnApiController;
import com.example.demo.controller.SheetController;
import com.example.demo.controller.TopController;
import com.example.demo.dto.SheetSearchResponse;
import com.example.demo.entity.SlipDetail;
import com.example.demo.mapper.SlipDetailMapper;
import com.example.demo.mapper.SlipMapper;
import com.example.demo.mapper.SlipMediaMapper;
import com.example.demo.mapper.TodoMapper;
import com.example.demo.service.GoogleSheetsService;
import com.example.demo.service.SlipService;

@WebMvcTest({
        HelloController.class,
        TopController.class,
        ExcelExportController.class,
        SheetController.class,
        ReturnApiController.class
})
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
                .andExpect(view().name("search-stage1"))
                .andExpect(content().string(Matchers.containsString("saveButton")));
    }

    @Test
    void topPageLoads() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(Matchers.containsString("/search")));
    }

    @Test
    void returnPageLoads() throws Exception {
        mockMvc.perform(get("/return"))
                .andExpect(status().isOk())
                .andExpect(view().name("return-stage2"))
                .andExpect(content().string(Matchers.containsString("伝票No")));
    }

    @Test
    void searchReturnsMatchingSheetRow() throws Exception {
        given(googleSheetsService.findByCode("A001"))
                .willReturn(Optional.of(new SheetSearchResponse("A001", "商品A", "1000")));

        mockMvc.perform(get("/api/search").param("code", "A001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("A001"))
                .andExpect(jsonPath("$.name").value("商品A"))
                .andExpect(jsonPath("$.price").value("1000"));
    }

    @Test
    void returnApiReturnsSlipDetails() throws Exception {
        SlipDetail detail = new SlipDetail();
        detail.setId(1);
        detail.setCode("12345678");
        detail.setName("商品A");
        detail.setPrice(1000);
        detail.setTaxPrice(1100);
        detail.setCredit("山田");
        detail.setReturned(Boolean.TRUE);

        given(slipDetailMapper.findBySlipNo("202604001")).willReturn(List.of(detail));

        mockMvc.perform(get("/api/return").param("slipNo", "202604001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].code").value("12345678"))
                .andExpect(jsonPath("$[0].returned").value(true));
    }

    @Test
    void returnUpdateApiSavesCheckedState() throws Exception {
        mockMvc.perform(post("/api/return/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {"id": 1, "returned": true},
                                  {"id": 2, "returned": false}
                                ]
                                """))
                .andExpect(status().isOk());

        SlipDetail returnedDetail = new SlipDetail();
        returnedDetail.setId(1);
        returnedDetail.setReturned(Boolean.TRUE);
        returnedDetail.setReturnedDate(LocalDate.now());
        verify(slipDetailMapper).updateReturn(ArgumentMatchers.refEq(returnedDetail));

        SlipDetail unreturnedDetail = new SlipDetail();
        unreturnedDetail.setId(2);
        unreturnedDetail.setReturned(Boolean.FALSE);
        unreturnedDetail.setReturnedDate(null);
        verify(slipDetailMapper).updateReturn(ArgumentMatchers.refEq(unreturnedDetail));
    }
}
