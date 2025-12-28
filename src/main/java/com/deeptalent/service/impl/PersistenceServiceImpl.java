package com.deeptalent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.deeptalent.domain.ConversationEntity;
import com.deeptalent.domain.DeepTalentState;
import com.deeptalent.mapper.ConversationMapper;
import com.deeptalent.service.PersistenceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 持久化服务实现类
 * 负责将面试会话状态保存到数据库或从数据库加载
 */
@Service
public class PersistenceServiceImpl implements PersistenceService {

    private final ConversationMapper conversationMapper;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数
     *
     * @param conversationMapper 数据库访问接口
     * @param objectMapper       JSON 序列化工具
     */
    public PersistenceServiceImpl(ConversationMapper conversationMapper, ObjectMapper objectMapper) {
        this.conversationMapper = conversationMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 加载会话状态
     *
     * @param threadId 会话唯一标识符
     * @return 加载的状态对象，如果不存在则返回新的初始状态
     */
    @Override
    @Transactional(readOnly = true)
    public DeepTalentState loadState(String threadId) {
        // 尝试从数据库查找记录
        ConversationEntity entity = conversationMapper.selectById(threadId);
        if (entity != null) {
            try {
                // 反序列化 JSON 字符串为状态对象
                return objectMapper.readValue(entity.getStateJson(), DeepTalentState.class);
            } catch (Exception e) {
                // 如果解析失败（如数据损坏），记录日志并返回新状态
                // 实际生产中应记录 ERROR 日志
                return new DeepTalentState();
            }
        }
        // 如果数据库中不存在，返回一个新的空状态
        return new DeepTalentState();
    }

    /**
     * 保存会话状态
     *
     * @param threadId 会话唯一标识符
     * @param state    要保存的状态对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveState(String threadId, DeepTalentState state) {
        try {
            // 将状态对象序列化为 JSON 字符串
            String json = objectMapper.writeValueAsString(state);
            // 创建或更新实体对象
            ConversationEntity entity = new ConversationEntity(threadId, json);
            
            // 检查是否存在
            if (conversationMapper.selectById(threadId) != null) {
                conversationMapper.updateById(entity);
            } else {
                conversationMapper.insert(entity);
            }
            // 或者使用 insertOrUpdate 如果配置了
            // conversationMapper.insertOrUpdate(entity);
            
        } catch (Exception e) {
            // 保存失败抛出运行时异常
            throw new RuntimeException("Failed to save state for threadId: " + threadId, e);
        }
    }
}
