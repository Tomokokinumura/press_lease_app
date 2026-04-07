package com.example.demo.mapper;

import org.apache.ibatis.annotations.Param;

import com.example.demo.entity.SlipMedia;

public interface SlipMediaMapper {

    void insert(SlipMedia slipMedia);

    java.util.List<SlipMedia> findBySlipId(@Param("slipId") Integer slipId);

    void deleteBySlipId(@Param("slipId") Integer slipId);
}
