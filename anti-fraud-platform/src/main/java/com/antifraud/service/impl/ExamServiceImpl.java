package com.antifraud.service.impl;

import com.antifraud.common.BusinessException;
import com.antifraud.dto.ExamSubmitDTO;
import com.antifraud.entity.ExamAnswer;
import com.antifraud.entity.ExamQuestion;
import com.antifraud.entity.ExamRecord;
import com.antifraud.entity.SysUser;
import com.antifraud.mapper.ExamAnswerMapper;
import com.antifraud.mapper.ExamQuestionMapper;
import com.antifraud.mapper.ExamRecordMapper;
import com.antifraud.mapper.UserMapper;
import com.antifraud.service.ExamService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    private ExamQuestionMapper questionMapper;

    @Autowired
    private ExamRecordMapper recordMapper;

    @Autowired
    private ExamAnswerMapper answerMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public Map<String, Object> startExam(Long userId, Integer questionCount, Long categoryId) {
        ExamRecord record = new ExamRecord();
        record.setUserId(userId);
        record.setStartTime(LocalDateTime.now());
        recordMapper.insert(record);

        LambdaQueryWrapper<ExamQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamQuestion::getStatus, 1);
        if (categoryId != null) {
            wrapper.eq(ExamQuestion::getCategoryId, categoryId);
        }
        int limit = (questionCount != null && questionCount > 0) ? questionCount : 10;
        wrapper.last("ORDER BY RAND() LIMIT " + limit);

        List<ExamQuestion> questions = questionMapper.selectList(wrapper);
        List<Map<String, Object>> questionVOList = questions.stream().map(this::toQuestionVO).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("recordId", record.getId());
        result.put("questions", questionVOList);
        result.put("totalQuestions", questionVOList.size());
        result.put("startTime", record.getStartTime());

        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> submitExam(Long userId, ExamSubmitDTO submitDTO) {
        if (submitDTO == null || submitDTO.getRecordId() == null) {
            throw new BusinessException("测试记录ID不能为空");
        }

        ExamRecord record = recordMapper.selectById(submitDTO.getRecordId());
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("测试记录不存在或无权访问");
        }
        if (record.getSubmitTime() != null) {
            throw new BusinessException("该测试记录已交卷，请勿重复提交");
        }

        List<Map<String, Object>> answers = Optional.ofNullable(submitDTO.getAnswers()).orElse(Collections.emptyList());

        int correctCount = 0;
        BigDecimal totalScore = BigDecimal.ZERO;

        for (Map<String, Object> answerMap : answers) {
            if (answerMap == null || answerMap.get("questionId") == null) {
                continue;
            }
            Long questionId = Long.valueOf(answerMap.get("questionId").toString());
            String userAnswer = Objects.toString(answerMap.get("userAnswer"), "");

            ExamQuestion question = questionMapper.selectById(questionId);
            if (question == null) {
                continue;
            }
            boolean isCorrect = normalizeAnswer(userAnswer).equals(normalizeAnswer(question.getAnswer()));

            ExamAnswer examAnswer = new ExamAnswer();
            examAnswer.setRecordId(record.getId());
            examAnswer.setQuestionId(questionId);
            examAnswer.setUserAnswer(normalizeAnswer(userAnswer));
            examAnswer.setIsCorrect(isCorrect ? 1 : 0);
            answerMapper.insert(examAnswer);

            if (isCorrect) {
                correctCount++;
                int score = question.getQuestionType() == 2 ? 15 : 10;
                totalScore = totalScore.add(new BigDecimal(score));
            }
        }

        record.setTotalScore(totalScore);
        record.setTotalQuestions(answers.size());
        record.setCorrectCount(correctCount);
        record.setSubmitTime(LocalDateTime.now());
        record.setDuration((int) ((System.currentTimeMillis() - 
            record.getStartTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()) / 1000));
        recordMapper.updateById(record);

        Map<String, Object> result = new HashMap<>();
        result.put("record", record);
        result.put("correctCount", correctCount);
        result.put("totalQuestions", answers.size());
        result.put("score", totalScore);
        result.put("accuracy", answers.size() > 0 ?
                BigDecimal.valueOf(correctCount * 100.0).divide(BigDecimal.valueOf(answers.size()), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO);

        return result;
    }

    @Override
    public Page<ExamQuestion> getQuestionList(int pageNum, int pageSize, Integer type, Long categoryId, Integer difficulty) {
        Page<ExamQuestion> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ExamQuestion> wrapper = new LambdaQueryWrapper<>();

        if (type != null) {
            wrapper.eq(ExamQuestion::getQuestionType, type);
        }
        if (categoryId != null) {
            wrapper.eq(ExamQuestion::getCategoryId, categoryId);
        }
        if (difficulty != null) {
            wrapper.eq(ExamQuestion::getDifficulty, difficulty);
        }

        wrapper.orderByDesc(ExamQuestion::getId);
        return questionMapper.selectPage(page, wrapper);
    }

    @Override
    public ExamQuestion addQuestion(ExamQuestion question) {
        question.setCreateTime(LocalDateTime.now());
        questionMapper.insert(question);
        return question;
    }

    @Override
    public ExamQuestion updateQuestion(Long id, ExamQuestion question) {
        question.setId(id);
        questionMapper.updateById(question);
        return question;
    }

    @Override
    public void deleteQuestion(Long id) {
        questionMapper.deleteById(id);
    }

    @Override
    public Map<String, Object> getExamRecordById(Long recordId) {
        ExamRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("成绩记录不存在");
        }

        LambdaQueryWrapper<ExamAnswer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(ExamAnswer::getRecordId, recordId).orderByAsc(ExamAnswer::getId);
        List<ExamAnswer> answerList = answerMapper.selectList(answerWrapper);

        List<Long> questionIds = answerList.stream().map(ExamAnswer::getQuestionId).distinct().collect(Collectors.toList());
        Map<Long, ExamQuestion> questionMap = new HashMap<>();
        if (!questionIds.isEmpty()) {
            QueryWrapper<ExamQuestion> questionWrapper = new QueryWrapper<>();
            questionWrapper.in("id", questionIds);
            questionMap = questionMapper.selectList(questionWrapper).stream()
                    .collect(Collectors.toMap(ExamQuestion::getId, q -> q));
        }

        List<Map<String, Object>> answerDetailList = new ArrayList<>();
        for (ExamAnswer answer : answerList) {
            ExamQuestion question = questionMap.get(answer.getQuestionId());
            Map<String, Object> detail = new HashMap<>();
            detail.put("questionId", answer.getQuestionId());
            detail.put("userAnswer", answer.getUserAnswer());
            detail.put("isCorrect", answer.getIsCorrect());
            if (question != null) {
                detail.put("questionText", question.getQuestionText());
                detail.put("questionType", question.getQuestionType());
                detail.put("optionA", question.getOptionA());
                detail.put("optionB", question.getOptionB());
                detail.put("optionC", question.getOptionC());
                detail.put("optionD", question.getOptionD());
                detail.put("correctAnswer", question.getAnswer());
                detail.put("analysis", question.getAnalysis());
            }
            answerDetailList.add(detail);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("record", record);
        result.put("answers", answerDetailList);
        result.put("accuracy", record.getTotalQuestions() != null && record.getTotalQuestions() > 0
                ? BigDecimal.valueOf(record.getCorrectCount() * 100.0)
                .divide(BigDecimal.valueOf(record.getTotalQuestions()), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        return result;
    }

    @Override
    public Page<ExamRecord> getUserRecords(Long userId, int pageNum, int pageSize) {
        Page<ExamRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getUserId, userId)
               .isNotNull(ExamRecord::getSubmitTime)
               .orderByDesc(ExamRecord::getSubmitTime);
        return recordMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Map<String, Object>> getRanking(int topN) {
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(ExamRecord::getSubmitTime)
               .orderByDesc(ExamRecord::getTotalScore)
               .orderByAsc(ExamRecord::getDuration)
               .last("LIMIT " + topN);

        List<ExamRecord> records = recordMapper.selectList(wrapper);
        List<Long> userIds = records.stream().map(ExamRecord::getUserId).distinct().collect(Collectors.toList());
        final Map<Long, SysUser> userMap;
        if (!userIds.isEmpty()) {
            QueryWrapper<SysUser> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.in("id", userIds);
            userMap = userMapper.selectList(userQueryWrapper).stream()
                    .collect(Collectors.toMap(SysUser::getId, u -> u));
        } else {
            userMap = Collections.emptyMap();
        }

        int[] rank = {1};
        return records.stream().map(r -> {
            Map<String, Object> item = new HashMap<>();
            SysUser user = userMap.get(r.getUserId());
            item.put("rank", rank[0]++);
            item.put("recordId", r.getId());
            item.put("userId", r.getUserId());
            item.put("username", user != null ? user.getUsername() : null);
            item.put("realName", user != null ? user.getRealName() : null);
            item.put("score", r.getTotalScore());
            item.put("correctCount", r.getCorrectCount());
            item.put("totalQuestions", r.getTotalQuestions());
            item.put("duration", r.getDuration());
            item.put("submitTime", r.getSubmitTime());
            return item;
        }).collect(Collectors.toList());
    }

    private Map<String, Object> toQuestionVO(ExamQuestion question) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", question.getId());
        item.put("questionText", question.getQuestionText());
        item.put("questionType", question.getQuestionType());
        item.put("categoryId", question.getCategoryId());
        item.put("difficulty", question.getDifficulty());
        item.put("optionA", question.getOptionA());
        item.put("optionB", question.getOptionB());
        item.put("optionC", question.getOptionC());
        item.put("optionD", question.getOptionD());
        return item;
    }

    private String normalizeAnswer(String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            return "";
        }
        String normalized = answer.replace("，", ",").replace(" ", "").toUpperCase();
        if (!normalized.contains(",")) {
            return normalized;
        }
        return Arrays.stream(normalized.split(","))
                .filter(s -> !s.isEmpty())
                .sorted()
                .collect(Collectors.joining(","));
    }
}
