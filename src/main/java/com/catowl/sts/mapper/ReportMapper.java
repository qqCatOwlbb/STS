package com.catowl.sts.mapper;

import com.catowl.sts.model.entity.AnalysisReport;
import com.catowl.sts.model.entity.WaterQualityData;
import com.catowl.sts.model.entity.WaterSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReportMapper {
    /**
     * 1. 插入新的分析报告
     * (ID 将通过 useGeneratedKeys 回填到 report 对象中)
     */
    int insertReport(AnalysisReport report);

    /**
     * 2. 批量插入报告与数据的关联
     */
    int insertReportDataLink(@Param("reportId") Long reportId, @Param("dataIds")List<Long> dataIds);

    /**
     * 3. 根据 StrId 查找水源信息 (获取 Dify Conversation ID)
     */
    WaterSource findWaterSourceByStrId(@Param("sourceStrId") String sourceStrId);

    /**
     * 5. 根据水源 ID 查询最近的水质数据
     */
    List<WaterQualityData> findRecentDataBySourceId(@Param("sourceId") Long sourceId, @Param("limit") int limit);
}
