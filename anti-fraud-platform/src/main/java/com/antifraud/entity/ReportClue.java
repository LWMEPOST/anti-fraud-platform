package com.antifraud.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("report_clue")
public class ReportClue {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reporterId;
    private String title;
    private String fraudType;
    private String description;
    private String evidenceImages;
    private String contactPhone;
    private String contactName;
    private Integer status;
    private Long handlerId;
    private String handleResult;
    private LocalDateTime handleTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
