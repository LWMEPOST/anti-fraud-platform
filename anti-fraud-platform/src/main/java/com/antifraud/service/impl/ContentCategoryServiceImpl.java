package com.antifraud.service.impl;

import com.antifraud.common.BusinessException;
import com.antifraud.common.ErrorCode;
import com.antifraud.entity.ContentCategory;
import com.antifraud.mapper.ContentCategoryMapper;
import com.antifraud.service.ContentCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContentCategoryServiceImpl implements ContentCategoryService {

    @Autowired
    private ContentCategoryMapper categoryMapper;

    @Override
    public List<ContentCategory> getAllCategories() {
        return categoryMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ContentCategory>()
                .eq(ContentCategory::getStatus, 1)
                .orderByAsc(ContentCategory::getSortOrder)
        );
    }

    @Override
    public ContentCategory getCategoryById(Long id) {
        ContentCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return category;
    }

    @Override
    public ContentCategory addCategory(ContentCategory category) {
        category.setCreateTime(LocalDateTime.now());
        categoryMapper.insert(category);
        return category;
    }

    @Override
    public ContentCategory updateCategory(Long id, ContentCategory category) {
        category.setId(id);
        categoryMapper.updateById(category);
        return category;
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
    }
}
