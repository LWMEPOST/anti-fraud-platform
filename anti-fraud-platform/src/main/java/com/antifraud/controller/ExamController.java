package com.antifraud.controller;

import com.antifraud.common.PageResult;
import com.antifraud.common.Result;
import com.antifraud.dto.ExamSubmitDTO;
import com.antifraud.entity.ExamQuestion;
import com.antifraud.entity.ExamRecord;
import com.antifraud.service.ExamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "在线测试")
@RestController
@RequestMapping("/exam")
public class ExamController {

    @Autowired
    private ExamService examService;

    @ApiOperation("开始测试（随机抽题）")
    @PostMapping("/start")
    public Result<Map<String, Object>> startExam(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") Integer questionCount,
            @RequestParam(required = false) Long categoryId) {
        return Result.success(examService.startExam(userId, questionCount, categoryId));
    }

    @ApiOperation("提交试卷（自动判分）")
    @PostMapping("/submit")
    public Result<Map<String, Object>> submitExam(@RequestParam Long userId, @RequestBody ExamSubmitDTO submitDTO) {
        return Result.success("提交成功", examService.submitExam(userId, submitDTO));
    }

    @ApiOperation("获取题库列表（管理员）")
    @GetMapping("/question/list")
    public Result<PageResult<ExamQuestion>> getQuestionList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer difficulty) {
        var page = examService.getQuestionList(pageNum, pageSize, type, categoryId, difficulty);
        return Result.success(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @ApiOperation("新增题目")
    @PostMapping("/question")
    public Result<ExamQuestion> addQuestion(@RequestBody ExamQuestion question) {
        return Result.success(examService.addQuestion(question));
    }

    @ApiOperation("更新题目")
    @PutMapping("/question/{id}")
    public Result<ExamQuestion> updateQuestion(@PathVariable Long id, @RequestBody ExamQuestion question) {
        return Result.success(examService.updateQuestion(id, question));
    }

    @ApiOperation("删除题目")
    @DeleteMapping("/question/{id}")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        examService.deleteQuestion(id);
        return Result.success();
    }

    @ApiOperation("查看成绩详情")
    @GetMapping("/record/{recordId}")
    public Result<Map<String, Object>> getExamRecord(@PathVariable Long recordId) {
        return Result.success(examService.getExamRecordById(recordId));
    }

    @ApiOperation("我的测试记录")
    @GetMapping("/records")
    public Result<PageResult<ExamRecord>> getUserRecords(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        var page = examService.getUserRecords(userId, pageNum, pageSize);
        return Result.success(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @ApiOperation("成绩排行榜")
    @GetMapping("/ranking")
    public Result<List<Map<String, Object>>> getRanking(@RequestParam(defaultValue = "10") int topN) {
        return Result.success(examService.getRanking(topN));
    }
}
