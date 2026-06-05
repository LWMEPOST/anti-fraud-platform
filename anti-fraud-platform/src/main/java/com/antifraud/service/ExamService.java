package com.antifraud.service;

import com.antifraud.dto.ExamSubmitDTO;
import com.antifraud.entity.ExamQuestion;
import com.antifraud.entity.ExamRecord;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Map;

public interface ExamService {
    Map<String, Object> startExam(Long userId, Integer questionCount, Long categoryId);
    
    Map<String, Object> submitExam(Long userId, ExamSubmitDTO submitDTO);
    
    Page<ExamQuestion> getQuestionList(int pageNum, int pageSize, Integer type, Long categoryId, Integer difficulty);
    
    ExamQuestion addQuestion(ExamQuestion question);
    
    ExamQuestion updateQuestion(Long id, ExamQuestion question);
    
    void deleteQuestion(Long id);
    
    Map<String, Object> getExamRecordById(Long recordId);
    
    Page<ExamRecord> getUserRecords(Long userId, int pageNum, int pageSize);
    
    List<Map<String, Object>> getRanking(int topN);
}
