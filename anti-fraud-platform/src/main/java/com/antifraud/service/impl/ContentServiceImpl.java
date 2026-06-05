package com.antifraud.service.impl;

import com.antifraud.common.BusinessException;
import com.antifraud.common.ErrorCode;
import com.antifraud.dto.ContentDTO;
import com.antifraud.dto.ContentVO;
import com.antifraud.entity.AntiFraudContent;
import com.antifraud.entity.ContentCategory;
import com.antifraud.mapper.AntiFraudContentMapper;
import com.antifraud.mapper.ContentCategoryMapper;
import com.antifraud.service.ContentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private AntiFraudContentMapper contentMapper;

    @Autowired
    private ContentCategoryMapper categoryMapper;

    @Override
    public Page<ContentVO> getContentList(int pageNum, int pageSize, Integer status, Long categoryId, String keyword) {
        Page<AntiFraudContent> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AntiFraudContent> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(AntiFraudContent::getStatus, status);
        }
        if (categoryId != null) {
            wrapper.eq(AntiFraudContent::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(AntiFraudContent::getTitle, keyword);
        }

        wrapper.orderByDesc(AntiFraudContent::getCreateTime);
        Page<AntiFraudContent> contentPage = contentMapper.selectPage(page, wrapper);

        // 转换为VO，包含分类名称
        List<ContentVO> voList = convertToVOList(contentPage.getRecords());

        Page<ContentVO> voPage = new Page<>(contentPage.getCurrent(), contentPage.getSize(), contentPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public ContentVO getContentById(Long id) {
        AntiFraudContent content = contentMapper.selectById(id);
        if (content == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return convertToVO(content);
    }

    @Override
    public ContentVO addContent(ContentDTO contentDTO) {
        AntiFraudContent content = new AntiFraudContent();
        content.setTitle(contentDTO.getTitle());
        content.setContentType(contentDTO.getContentType());
        content.setContent(contentDTO.getContent());
        content.setCoverImage(contentDTO.getCoverImage());
        content.setVideoUrl(contentDTO.getVideoUrl());
        content.setCategoryId(contentDTO.getCategoryId());
        content.setStatus(0);
        content.setViewCount(0);
        content.setLikeCount(0);
        LocalDateTime now = LocalDateTime.now();
        content.setCreateTime(now);
        content.setUpdateTime(now);
        contentMapper.insert(content);
        return convertToVO(content);
    }

    @Override
    public ContentVO updateContent(Long id, ContentDTO contentDTO) {
        AntiFraudContent content = getContentEntityById(id);

        if (StringUtils.hasText(contentDTO.getTitle())) {
            content.setTitle(contentDTO.getTitle());
        }
        if (contentDTO.getContentType() != null) {
            content.setContentType(contentDTO.getContentType());
        }
        if (contentDTO.getContent() != null) {
            content.setContent(contentDTO.getContent());
        }
        if (contentDTO.getCoverImage() != null) {
            content.setCoverImage(contentDTO.getCoverImage());
        }
        if (contentDTO.getVideoUrl() != null) {
            content.setVideoUrl(contentDTO.getVideoUrl());
        }
        if (contentDTO.getCategoryId() != null) {
            content.setCategoryId(contentDTO.getCategoryId());
        }

        contentMapper.updateById(content);
        return convertToVO(content);
    }

    @Override
    @Transactional
    public void deleteContent(Long id) {
        contentMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void auditContent(Long id, Integer status) {
        AntiFraudContent content = getContentEntityById(id);

        if (!isValidStatusTransition(content.getStatus(), status)) {
            throw new BusinessException(ErrorCode.CONTENT_AUDIT_FAIL);
        }

        LambdaUpdateWrapper<AntiFraudContent> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AntiFraudContent::getId, id)
                     .set(AntiFraudContent::getStatus, status);

        if (status == 1) {
            updateWrapper.set(AntiFraudContent::getPublishTime, LocalDateTime.now());
        }

        contentMapper.update(null, updateWrapper);
    }

    @Override
    public Page<ContentVO> getPublishedContent(int pageNum, int pageSize, Long categoryId) {
        Page<AntiFraudContent> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AntiFraudContent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AntiFraudContent::getStatus, 1)
               .orderByDesc(AntiFraudContent::getPublishTime);

        if (categoryId != null) {
            wrapper.eq(AntiFraudContent::getCategoryId, categoryId);
        }

        Page<AntiFraudContent> contentPage = contentMapper.selectPage(page, wrapper);
        List<ContentVO> voList = convertToVOList(contentPage.getRecords());

        Page<ContentVO> voPage = new Page<>(contentPage.getCurrent(), contentPage.getSize(), contentPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public void incrementViewCount(Long id) {
        contentMapper.incrementViewCount(id);
    }

    @Override
    public void likeContent(Long id) {
        contentMapper.incrementLikeCount(id);
    }

    @Override
    public List<ContentVO> getLatestContent(int limit) {
        LambdaQueryWrapper<AntiFraudContent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AntiFraudContent::getStatus, 1)
               .orderByDesc(AntiFraudContent::getPublishTime)
               .last("LIMIT " + limit);

        List<AntiFraudContent> contents = contentMapper.selectList(wrapper);
        return convertToVOList(contents);
    }

    // 辅助方法：根据ID获取内容实体
    private AntiFraudContent getContentEntityById(Long id) {
        AntiFraudContent content = contentMapper.selectById(id);
        if (content == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return content;
    }

    // 辅助方法：状态转换验证
    private boolean isValidStatusTransition(Integer fromStatus, Integer toStatus) {
        if (fromStatus == 0 && (toStatus == 1 || toStatus == 2)) return true;
        if (fromStatus == 1 && toStatus == 2) return true;
        return false;
    }

    // 辅助方法：转换为VO，包含分类名称
    private ContentVO convertToVO(AntiFraudContent content) {
        ContentVO vo = new ContentVO();
        BeanUtils.copyProperties(content, vo);

        // 设置内容类型名称
        vo.setContentTypeName(getContentTypeName(content.getContentType()));

        // 查询分类名称
        if (content.getCategoryId() != null) {
            ContentCategory category = categoryMapper.selectById(content.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }
        }

        return vo;
    }

    // 辅助方法：批量转换为VO
    private List<ContentVO> convertToVOList(List<AntiFraudContent> contents) {
        // 批量获取分类信息
        List<Long> categoryIds = contents.stream()
            .filter(c -> c.getCategoryId() != null)
            .map(AntiFraudContent::getCategoryId)
            .distinct()
            .collect(Collectors.toList());

        Map<Long, String> categoryMap = categoryIds.isEmpty() ? 
            new java.util.HashMap<>() :
            categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(ContentCategory::getId, ContentCategory::getCategoryName));

        return contents.stream().map(content -> {
            ContentVO vo = new ContentVO();
            BeanUtils.copyProperties(content, vo);
            vo.setContentTypeName(getContentTypeName(content.getContentType()));
            vo.setCategoryName(categoryMap.get(content.getCategoryId()));
            return vo;
        }).collect(Collectors.toList());
    }

    // 辅助方法：获取内容类型名称
    private String getContentTypeName(Integer type) {
        switch (type) {
            case 1: return "文章";
            case 2: return "视频";
            case 3: return "漫画";
            default: return "未知";
        }
    }
}
