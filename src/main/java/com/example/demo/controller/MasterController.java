package com.example.demo.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.entity.MasterSetting;
import com.example.demo.mapper.MasterSettingMapper;

@Controller
public class MasterController {

    private static final String DEFAULT_MASTER_TEXT = "ここにExcelに表示する固定文言を登録してください。";
    private static final String CREATE_MASTER_SETTING_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS master_setting (
                id SERIAL PRIMARY KEY,
                master_text VARCHAR(1000),
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;

    private final MasterSettingMapper masterSettingMapper;
    private final JdbcTemplate jdbcTemplate;

    public MasterController(MasterSettingMapper masterSettingMapper, JdbcTemplate jdbcTemplate) {
        this.masterSettingMapper = masterSettingMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/master")
    public String show(Model model) {
        model.addAttribute("master", getOrCreateSetting());
        return "master";
    }

    @PostMapping("/master/update")
    public String update(@ModelAttribute MasterSetting master) {
        MasterSetting current = getOrCreateSetting();
        current.setMasterText(master.getMasterText());
        masterSettingMapper.update(current);
        return "redirect:/master?saved=1";
    }

    private MasterSetting getOrCreateSetting() {
        ensureMasterSettingTable();

        MasterSetting setting = masterSettingMapper.find();
        if (setting != null) {
            return setting;
        }

        MasterSetting initial = new MasterSetting();
        initial.setMasterText(DEFAULT_MASTER_TEXT);
        masterSettingMapper.insert(initial);
        return masterSettingMapper.find();
    }

    private void ensureMasterSettingTable() {
        jdbcTemplate.execute(CREATE_MASTER_SETTING_TABLE_SQL);
    }
}