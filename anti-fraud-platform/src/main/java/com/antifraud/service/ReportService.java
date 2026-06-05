package com.antifraud.service;

import com.antifraud.dto.ReportDTO;
import com.antifraud.entity.ReportClue;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Map;

public interface ReportService {
    ReportClue submitReport(ReportDTO reportDTO);
    
    Page<ReportClue> getReportList(int pageNum, int pageSize, Integer status, String keyword);
    
    ReportClue getReportById(Long id);
    
    void handleReport(Long id, Long handlerId, Integer status, String handleResult);
    
    Page<ReportClue> getMyReports(Long reporterId, int pageNum, int pageSize);
    
    Map<String, Object> getStatistics();
    
    List<Map<String, Object>> getStatusDistribution();
}
