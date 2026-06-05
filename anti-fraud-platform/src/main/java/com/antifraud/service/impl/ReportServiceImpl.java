package com.antifraud.service.impl;

import com.antifraud.common.BusinessException;
import com.antifraud.common.ErrorCode;
import com.antifraud.dto.ReportDTO;
import com.antifraud.entity.ReportClue;
import com.antifraud.mapper.ReportClueMapper;
import com.antifraud.service.ReportService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportClueMapper reportClueMapper;

    @Override
    public ReportClue submitReport(ReportDTO reportDTO) {
        LocalDateTime now = LocalDateTime.now();
        ReportClue clue = new ReportClue();
        clue.setReporterId(reportDTO.getReporterId());
        clue.setTitle(reportDTO.getTitle());
        clue.setFraudType(reportDTO.getFraudType());
        clue.setDescription(reportDTO.getDescription());
        clue.setEvidenceImages(reportDTO.getEvidenceImages());
        clue.setContactPhone(reportDTO.getContactPhone());
        clue.setContactName(reportDTO.getContactName());
        clue.setStatus(0);
        clue.setCreateTime(now);
        clue.setUpdateTime(now);
        reportClueMapper.insert(clue);
        return clue;
    }

    @Override
    public Page<ReportClue> getReportList(int pageNum, int pageSize, Integer status, String keyword) {
        Page<ReportClue> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ReportClue> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(ReportClue::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ReportClue::getTitle, keyword)
                    .or()
                    .like(ReportClue::getFraudType, keyword));
        }

        wrapper.orderByDesc(ReportClue::getCreateTime);
        return reportClueMapper.selectPage(page, wrapper);
    }

    @Override
    public ReportClue getReportById(Long id) {
        ReportClue clue = reportClueMapper.selectById(id);
        if (clue == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return clue;
    }

    @Override
    @Transactional
    public void handleReport(Long id, Long handlerId, Integer status, String handleResult) {
        ReportClue clue = getReportById(id);

        if (!canTransitionStatus(clue.getStatus(), status)) {
            throw new BusinessException(ErrorCode.REPORT_STATUS_ERROR);
        }

        LambdaUpdateWrapper<ReportClue> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ReportClue::getId, id)
                     .set(ReportClue::getStatus, status)
                     .set(ReportClue::getHandlerId, handlerId)
                     .set(ReportClue::getHandleResult, handleResult)
                     .set(ReportClue::getHandleTime, LocalDateTime.now());

        reportClueMapper.update(null, updateWrapper);
    }

    private boolean canTransitionStatus(Integer fromStatus, Integer toStatus) {
        switch (fromStatus) {
            case 0:
                return toStatus == 1 || toStatus == 3;
            case 1:
                return toStatus == 2 || toStatus == 3;
            default:
                return false;
        }
    }

    @Override
    public Page<ReportClue> getMyReports(Long reporterId, int pageNum, int pageSize) {
        Page<ReportClue> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ReportClue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportClue::getReporterId, reporterId)
               .orderByDesc(ReportClue::getCreateTime);
        return reportClueMapper.selectPage(page, wrapper);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long total = reportClueMapper.selectCount(null);

        long pending = reportClueMapper.selectCount(
            new LambdaQueryWrapper<ReportClue>().eq(ReportClue::getStatus, 0)
        );
        long processing = reportClueMapper.selectCount(
            new LambdaQueryWrapper<ReportClue>().eq(ReportClue::getStatus, 1)
        );
        long resolved = reportClueMapper.selectCount(
            new LambdaQueryWrapper<ReportClue>().eq(ReportClue::getStatus, 2)
        );
        long closed = reportClueMapper.selectCount(
            new LambdaQueryWrapper<ReportClue>().eq(ReportClue::getStatus, 3)
        );

        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("processing", processing);
        stats.put("resolved", resolved);
        stats.put("closed", closed);

        if (total > 0) {
            stats.put("resolveRate", BigDecimal.valueOf(resolved * 100L)
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
        } else {
            stats.put("resolveRate", BigDecimal.ZERO);
        }

        return stats;
    }

    @Override
    public List<Map<String, Object>> getStatusDistribution() {
        List<Map<String, Object>> distribution = new ArrayList<>();
        
        Map<String, Integer> statusMap = new LinkedHashMap<>();
        statusMap.put("待处理", 0);
        statusMap.put("处理中", 1);
        statusMap.put("已解决", 2);
        statusMap.put("已关闭", 3);

        for (Map.Entry<String, Integer> entry : statusMap.entrySet()) {
            long count = reportClueMapper.selectCount(
                new LambdaQueryWrapper<ReportClue>().eq(ReportClue::getStatus, entry.getValue())
            );

            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("value", count);
            item.put("status", entry.getValue());
            distribution.add(item);
        }

        return distribution;
    }
}
