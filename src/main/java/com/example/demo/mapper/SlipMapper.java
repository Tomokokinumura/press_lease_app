package com.example.demo.mapper;

import org.apache.ibatis.annotations.Param;

import com.example.demo.entity.Slip;

public interface SlipMapper {

    String findLatestSlipNoByMonth(@Param("ym") String ym);

    void insert(Slip slip);

    Slip findBySlipNo(@Param("slipNo") String slipNo);

    Integer findIdBySlipNo(@Param("slipNo") String slipNo);

    void updateSlip(Slip slip);

    void updateHeader(Slip slip);

    void delete(@Param("id") Integer id);
}
