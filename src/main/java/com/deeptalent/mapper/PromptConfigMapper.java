package com.deeptalent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deeptalent.domain.PromptConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提示词配置 Mapper 接口
 *
 * @author 小阳
 * @date 2025-12-28
 * @version 1.0.0
 */
@Mapper
public interface PromptConfigMapper extends BaseMapper<PromptConfigEntity> {
}
