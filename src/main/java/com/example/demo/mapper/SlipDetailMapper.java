package com.example.demo.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.example.demo.dto.SlipDetailDto;
import com.example.demo.entity.SlipDetail;

public interface SlipDetailMapper {

    void insert(SlipDetail slipDetail);

    List<SlipDetail> findBySlipNo(@Param("slipNo") String slipNo);

    void updateReturn(SlipDetail slipDetail);

    List<SlipDetailDto> findSlipRowsBySlipNo(@Param("slipNo") String slipNo);

    List<SlipDetailDto> findSlipRowsBySlipNoWithLoanDateRange(
            @Param("slipNo") String slipNo,
            @Param("loanDateFrom") LocalDate loanDateFrom,
            @Param("loanDateTo") LocalDate loanDateTo);

    List<SlipDetailDto> findSlipRowsByCode(@Param("code") String code);

    List<SlipDetailDto> findSlipRowsByCodeWithLoanDateRange(
            @Param("code") String code,
            @Param("loanDateFrom") LocalDate loanDateFrom,
            @Param("loanDateTo") LocalDate loanDateTo);

    void updateDetail(SlipDetailDto detail);

    void deleteById(@Param("id") Integer id);

    void deleteBySlipId(@Param("slipId") Integer slipId);
}
