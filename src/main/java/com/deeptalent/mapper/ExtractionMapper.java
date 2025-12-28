package com.deeptalent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deeptalent.domain.ExtractionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 特征提取数据访问层
 */
@Mapper
public interface ExtractionMapper extends BaseMapper<ExtractionEntity> {
}
