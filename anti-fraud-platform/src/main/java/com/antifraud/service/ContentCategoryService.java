package com.antifraud.service;

import com.antifraud.entity.ContentCategory;
import java.util.List;

public interface ContentCategoryService {
    List<ContentCategory> getAllCategories();
    
    ContentCategory getCategoryById(Long id);
    
    ContentCategory addCategory(ContentCategory category);
    
    ContentCategory updateCategory(Long id, ContentCategory category);
    
    void deleteCategory(Long id);
}
