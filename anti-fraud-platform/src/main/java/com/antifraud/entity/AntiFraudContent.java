package com.antifraud.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("anti_fraud_content")
public class AntiFraudContent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private Integer contentType;
    private String content;
    private String coverImage;
    private String videoUrl;
    private Long categoryId;
    private Long authorId;
    private Integer status;
    private Integer viewCount;
    private Integer likeCount;
    private LocalDateTime publishTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
