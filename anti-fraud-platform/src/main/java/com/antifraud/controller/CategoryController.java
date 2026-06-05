package com.antifraud.controller;

import com.antifraud.common.Result;
import com.antifraud.entity.ContentCategory;
import com.antifraud.service.ContentCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "内容分类管理")
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private ContentCategoryService categoryService;

    @ApiOperation("获取分类列表")
    @GetMapping("/list")
    public Result<List<ContentCategory>> list() {
        return Result.success(categoryService.getAllCategories());
    }

    @ApiOperation("获取所有分类")
    @GetMapping("/all")
    public Result<List<ContentCategory>> getAll() {
        return Result.success(categoryService.getAllCategories());
    }

    @ApiOperation("根据ID查询分类")
    @GetMapping("/{id}")
    public Result<ContentCategory> getById(@PathVariable Long id) {
        return Result.success(categoryService.getCategoryById(id));
    }

    @ApiOperation("新增分类")
    @PostMapping
    public Result<ContentCategory> add(@RequestBody ContentCategory category) {
        return Result.success(categoryService.addCategory(category));
    }

    @ApiOperation("更新分类")
    @PutMapping("/{id}")
    public Result<ContentCategory> update(@PathVariable Long id, @RequestBody ContentCategory category) {
        return Result.success(categoryService.updateCategory(id, category));
    }

    @ApiOperation("删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
}
