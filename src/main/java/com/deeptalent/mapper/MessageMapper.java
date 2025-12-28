package com.deeptalent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.deeptalent.domain.MessageEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息数据访问层
 */
@Mapper
public interface MessageMapper extends BaseMapper<MessageEntity> {
}
