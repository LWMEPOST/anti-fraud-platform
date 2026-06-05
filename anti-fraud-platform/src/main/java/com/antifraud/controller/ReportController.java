package com.antifraud.controller;

import com.antifraud.common.PageResult;
import com.antifraud.common.Result;
import com.antifraud.dto.ReportDTO;
import com.antifraud.entity.ReportClue;
import com.antifraud.service.ReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "举报管理")
@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @ApiOperation("提交举报")
    @PostMapping
    public Result<ReportClue> submit(@RequestBody ReportDTO reportDTO) {
        return Result.success("举报已提交，我们将尽快处理", reportService.submitReport(reportDTO));
    }

    @ApiOperation("获取举报列表（管理员）")
    @GetMapping("/list")
    public Result<PageResult<ReportClue>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        var page = reportService.getReportList(pageNum, pageSize, status, keyword);
        return Result.success(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @ApiOperation("根据ID查询举报详情")
    @GetMapping("/{id}")
    public Result<ReportClue> getById(@PathVariable Long id) {
        return Result.success(reportService.getReportById(id));
    }

    @ApiOperation("处理举报（状态流转）")
    @PutMapping("/{id}/handle")
    public Result<Void> handle(
            @PathVariable Long id,
            @RequestParam Long handlerId,
            @RequestParam Integer status,
            @RequestParam(required = false) String handleResult) {
        reportService.handleReport(id, handlerId, status, handleResult);
        return Result.success("处理成功");
    }

    @ApiOperation("我的举报记录")
    @GetMapping("/mine")
    public Result<PageResult<ReportClue>> myReports(
            @RequestParam Long reporterId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        var page = reportService.getMyReports(reporterId, pageNum, pageSize);
        return Result.success(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @ApiOperation("举报统计数据")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        return Result.success(reportService.getStatistics());
    }

    @ApiOperation("举报状态分布")
    @GetMapping("/distribution")
    public Result<List<Map<String, Object>>> distribution() {
        return Result.success(reportService.getStatusDistribution());
    }
}
