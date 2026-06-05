package com.antifraud.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ReportDTO {
    @NotNull(message = "举报人ID不能为空")
    private Long reporterId;
    
    @NotBlank(message = "标题不能为空")
    private String title;
    
    private String fraudType;
    private String description;
    private String evidenceImages;
    private String contactPhone;
    private String contactName;
}
