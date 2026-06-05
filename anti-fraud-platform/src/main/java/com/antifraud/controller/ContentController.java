package com.antifraud.controller;

import com.antifraud.common.PageResult;
import com.antifraud.common.Result;
import com.antifraud.dto.ContentDTO;
import com.antifraud.dto.ContentVO;
import com.antifraud.service.ContentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "内容管理")
@RestController
@RequestMapping("/content")
public class ContentController {

    @Autowired
    private ContentService contentService;

    @ApiOperation("获取内容列表（管理员）")
    @GetMapping("/list")
    public Result<PageResult<ContentVO>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        var page = contentService.getContentList(pageNum, pageSize, status, categoryId, keyword);
        return Result.success(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @ApiOperation("获取已上线内容（前端展示）")
    @GetMapping("/published")
    public Result<PageResult<ContentVO>> published(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long categoryId) {
        var page = contentService.getPublishedContent(pageNum, pageSize, categoryId);
        return Result.success(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @ApiOperation("获取最新内容（用于首页展示）")
    @GetMapping("/latest")
    public Result<List<ContentVO>> latest(@RequestParam(defaultValue = "6") int limit) {
        return Result.success(contentService.getLatestContent(limit));
    }

    @ApiOperation("根据ID查询内容详情")
    @GetMapping("/{id}")
    public Result<ContentVO> getById(@PathVariable Long id) {
        ContentVO content = contentService.getContentById(id);
        contentService.incrementViewCount(id);
        return Result.success(content);
    }

    @ApiOperation("新增内容")
    @PostMapping
    public Result<ContentVO> add(@RequestBody ContentDTO contentDTO) {
        return Result.success(contentService.addContent(contentDTO));
    }

    @ApiOperation("更新内容")
    @PutMapping("/{id}")
    public Result<ContentVO> update(@PathVariable Long id, @RequestBody ContentDTO contentDTO) {
        return Result.success(contentService.updateContent(id, contentDTO));
    }

    @ApiOperation("删除内容")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        contentService.deleteContent(id);
        return Result.success();
    }

    @ApiOperation("内容审核（上线/下架）")
    @PutMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestParam Integer status) {
        contentService.auditContent(id, status);
        String msg = status == 1 ? "已上线" : "已下架";
        return Result.success(msg);
    }

    @ApiOperation("点赞")
    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        contentService.likeContent(id);
        return Result.success();
    }
}
