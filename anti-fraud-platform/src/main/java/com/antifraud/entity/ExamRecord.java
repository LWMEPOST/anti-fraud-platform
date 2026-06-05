package com.antifraud.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exam_record")
public class ExamRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal totalScore;
    private Integer totalQuestions;
    private Integer correctCount;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private Integer duration;
}
