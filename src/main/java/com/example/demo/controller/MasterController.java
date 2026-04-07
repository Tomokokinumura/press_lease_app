package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.entity.MasterSetting;
import com.example.demo.mapper.MasterSettingMapper;

@Controller
public class MasterController {

    private static final String DEFAULT_MASTER_TEXT = "ここにExcelに表示する固定文言を入力";

    private final MasterSettingMapper masterSettingMapper;

    public MasterController(MasterSettingMapper masterSettingMapper) {
        this.masterSettingMapper = masterSettingMapper;
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
        return "redirect:/master";
    }

    private MasterSetting getOrCreateSetting() {
        MasterSetting setting = masterSettingMapper.find();
        if (setting != null) {
            return setting;
        }

        MasterSetting initial = new MasterSetting();
        initial.setMasterText(DEFAULT_MASTER_TEXT);
        masterSettingMapper.insert(initial);
        return masterSettingMapper.find();
    }
}
