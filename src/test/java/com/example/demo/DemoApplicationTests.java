package com.example.demo;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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
import com.example.demo.controller.MasterController;
import com.example.demo.controller.ReturnApiController;
import com.example.demo.controller.SheetController;
import com.example.demo.controller.SlipExcelController;
import com.example.demo.controller.SlipSearchApiController;
import com.example.demo.controller.TopController;
import com.example.demo.dto.SheetSearchResponse;
import com.example.demo.dto.SlipDetailDto;
import com.example.demo.dto.SlipEditResponse;
import com.example.demo.entity.MasterSetting;
import com.example.demo.entity.SlipDetail;
import com.example.demo.mapper.MasterSettingMapper;
import com.example.demo.mapper.SlipDetailMapper;
import com.example.demo.mapper.SlipMapper;
import com.example.demo.mapper.SlipMediaMapper;
import com.example.demo.mapper.TodoMapper;
import com.example.demo.service.GoogleSheetsService;
import com.example.demo.service.SlipExcelService;
import com.example.demo.service.SlipService;

@WebMvcTest({
        HelloController.class,
        TopController.class,
        MasterController.class,
        ExcelExportController.class,
        SlipExcelController.class,
        SheetController.class,
        ReturnApiController.class,
        SlipSearchApiController.class
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
    private MasterSettingMapper masterSettingMapper;

    @MockBean
    private GoogleSheetsService googleSheetsService;

    @MockBean
    private SlipService slipService;

    @MockBean
    private SlipExcelService slipExcelService;

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
                .andExpect(content().string(Matchers.containsString("slipNo")));
    }

    @Test
    void slipSearchPageLoads() throws Exception {
        mockMvc.perform(get("/slip-search"))
                .andExpect(status().isOk())
                .andExpect(view().name("slip-search-stage3"))
                .andExpect(content().string(Matchers.containsString("searchSlipButton")));
    }

    @Test
    void masterPageLoads() throws Exception {
        MasterSetting setting = new MasterSetting();
        setting.setId(1);
        setting.setMasterText("固定文言");
        given(masterSettingMapper.find()).willReturn(setting);

        mockMvc.perform(get("/master"))
                .andExpect(status().isOk())
                .andExpect(view().name("master"))
                .andExpect(model().attributeExists("master"))
                .andExpect(content().string(Matchers.containsString("masterText")));
    }

    @Test
    void masterUpdateRedirectsToMasterPage() throws Exception {
        MasterSetting setting = new MasterSetting();
        setting.setId(1);
        setting.setMasterText("before");
        given(masterSettingMapper.find()).willReturn(setting);

        mockMvc.perform(post("/master/update")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", "1")
                        .param("masterText", "updated text"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/master"));

        verify(masterSettingMapper).update(ArgumentMatchers.argThat(master ->
                master.getId() == 1 && "updated text".equals(master.getMasterText())));
    }

    @Test
    void searchReturnsMatchingSheetRow() throws Exception {
        given(googleSheetsService.findByCode("A001"))
                .willReturn(Optional.of(new SheetSearchResponse("A001", "Product A", "1000")));

        mockMvc.perform(get("/api/search").param("code", "A001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("A001"))
                .andExpect(jsonPath("$.name").value("Product A"))
                .andExpect(jsonPath("$.price").value("1000"));
    }

    @Test
    void returnApiReturnsSlipDetails() throws Exception {
        SlipDetail detail = new SlipDetail();
        detail.setId(1);
        detail.setCode("12345678");
        detail.setName("Product A");
        detail.setPrice(1000);
        detail.setTaxPrice(1100);
        detail.setCredit("Yamada");
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

    @Test
    void slipApiReturnsRowsBySlipNo() throws Exception {
        SlipDetailDto dto = new SlipDetailDto();
        dto.setSlipId(10);
        dto.setSlipNo("202604001");
        dto.setId(1);
        dto.setCode("12345678");
        dto.setName("Product A");
        dto.setReturned(Boolean.TRUE);

        given(slipService.findBySlipNo("202604001")).willReturn(List.of(dto));

        mockMvc.perform(get("/api/slip").param("slipNo", "202604001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slipNo").value("202604001"))
                .andExpect(jsonPath("$[0].code").value("12345678"))
                .andExpect(jsonPath("$[0].returned").value(true));
    }

    @Test
    void slipCodeApiReturnsRowsByCode() throws Exception {
        SlipDetailDto dto = new SlipDetailDto();
        dto.setSlipId(10);
        dto.setSlipNo("202604001");
        dto.setId(1);
        dto.setCode("12345678");
        dto.setName("Product A");

        given(slipService.findByCode("12345678")).willReturn(List.of(dto));

        mockMvc.perform(get("/api/slip/code").param("code", "12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slipNo").value("202604001"))
                .andExpect(jsonPath("$[0].code").value("12345678"));
    }

    @Test
    void slipEditApiReturnsSlipForEditing() throws Exception {
        SlipEditResponse response = new SlipEditResponse();
        response.setSlipNo("202604001");
        response.setStaffName("担当者A");

        SlipDetail detail = new SlipDetail();
        detail.setCode("12345678");
        detail.setName("Product A");
        response.setDetails(List.of(detail));

        given(slipService.loadSlipForEdit("202604001")).willReturn(response);

        mockMvc.perform(get("/api/slip/edit").param("slipNo", "202604001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slipNo").value("202604001"))
                .andExpect(jsonPath("$.staffName").value("担当者A"))
                .andExpect(jsonPath("$.details[0].code").value("12345678"));
    }

    @Test
    void slipUpdateApiDelegatesToService() throws Exception {
        mockMvc.perform(post("/api/slip/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {
                                    "id": 1,
                                    "slipId": 10,
                                    "slipNo": "202604001",
                                    "code": "12345678",
                                    "name": "Product A",
                                    "price": 1000,
                                    "taxPrice": 1100,
                                    "credit": "Yamada",
                                    "mediaName": "Magazine",
                                    "releaseDate": "2026-04-01",
                                    "loanDate": "2026-04-07",
                                    "returnDate": "2026-04-20",
                                    "note": "memo",
                                    "returned": true
                                  }
                                ]
                                """))
                .andExpect(status().isOk());

        verify(slipService).updateDetails(ArgumentMatchers.anyList());
    }

    @Test
    void slipDeleteApiDelegatesToService() throws Exception {
        mockMvc.perform(post("/api/slip/delete").param("slipNo", "202604001"))
                .andExpect(status().isOk());

        verify(slipService).deleteBySlipNo("202604001");
    }

    @Test
    void excelTemplateExportDelegatesToService() throws Exception {
        mockMvc.perform(get("/excel/export/202604001"))
                .andExpect(status().isOk());

        verify(slipExcelService).exportSlip(ArgumentMatchers.eq("202604001"), ArgumentMatchers.any());
    }
}
