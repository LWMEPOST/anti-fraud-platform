package com.antifraud.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ContentVO {
    private Long id;
    private String title;
    private Integer contentType;
    private String contentTypeName;
    private String content;
    private String coverImage;
    private String videoUrl;
    private Long categoryId;
    private String categoryName;
    private Long authorId;
    private Integer status;
    private Integer viewCount;
    private Integer likeCount;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
}
