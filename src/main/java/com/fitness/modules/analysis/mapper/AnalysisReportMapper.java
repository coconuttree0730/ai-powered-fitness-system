package com.fitness.modules.analysis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.analysis.model.entity.AnalysisReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI数据分析报告Mapper接口
 */
@Mapper
public interface AnalysisReportMapper extends BaseMapper<AnalysisReport> {
}
