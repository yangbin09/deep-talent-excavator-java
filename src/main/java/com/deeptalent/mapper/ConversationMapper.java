package com.deeptalent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deeptalent.domain.ConversationEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话数据访问层接口
 * 使用 MyBatis-Plus 提供的 BaseMapper 简化数据库操作
 *
 * @author 小阳
 * @date 2025-12-28
 * @version 1.0.0
 */
@Mapper
public interface ConversationMapper extends BaseMapper<ConversationEntity> {
}
