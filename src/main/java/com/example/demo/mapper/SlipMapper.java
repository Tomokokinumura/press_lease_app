package com.example.demo.mapper;

import org.apache.ibatis.annotations.Param;

import com.example.demo.entity.Slip;

public interface SlipMapper {

    Integer countByMonth(@Param("ym") String ym);

    void insert(Slip slip);

    Integer findIdBySlipNo(@Param("slipNo") String slipNo);

    void updateHeader(Slip slip);

    void delete(@Param("id") Integer id);
}
