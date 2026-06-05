package com.antifraud.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ContentDTO {
    private Long id;
    
    @NotBlank(message = "标题不能为空")
    private String title;
    
    @NotNull(message = "内容类型不能为空")
    private Integer contentType;
    
    private String content;
    private String coverImage;
    private String videoUrl;
    private Long categoryId;
    private Integer status;
}
