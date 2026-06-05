package com.antifraud.mapper;

import com.antifraud.entity.AntiFraudContent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AntiFraudContentMapper extends BaseMapper<AntiFraudContent> {
    
    @Update("UPDATE anti_fraud_content SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(Long id);
    
    @Update("UPDATE anti_fraud_content SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(Long id);
}
