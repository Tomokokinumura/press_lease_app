package com.example.demo.mapper;

import com.example.demo.entity.MasterSetting;

public interface MasterSettingMapper {

    MasterSetting find();

    void insert(MasterSetting setting);

    void update(MasterSetting setting);
}
