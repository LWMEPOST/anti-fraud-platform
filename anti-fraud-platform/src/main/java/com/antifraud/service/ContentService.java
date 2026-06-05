package com.antifraud.service;

import com.antifraud.dto.ContentDTO;
import com.antifraud.dto.ContentVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;

public interface ContentService {
    Page<ContentVO> getContentList(int pageNum, int pageSize, Integer status, Long categoryId, String keyword);
    
    ContentVO getContentById(Long id);
    
    ContentVO addContent(ContentDTO contentDTO);
    
    ContentVO updateContent(Long id, ContentDTO contentDTO);
    
    void deleteContent(Long id);
    
    void auditContent(Long id, Integer status);
    
    Page<ContentVO> getPublishedContent(int pageNum, int pageSize, Long categoryId);
    
    void incrementViewCount(Long id);
    
    void likeContent(Long id);
    
    List<ContentVO> getLatestContent(int limit);
}
