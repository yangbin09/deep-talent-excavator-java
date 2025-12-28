package com.deeptalent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.deeptalent.domain.*;
import com.deeptalent.mapper.ConversationMapper;
import com.deeptalent.mapper.ExtractionMapper;
import com.deeptalent.mapper.MessageMapper;
import com.deeptalent.service.PersistenceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 持久化服务实现类 (重构版)
 * 适配新的关系型数据库结构
 */
@Service
public class PersistenceServiceImpl implements PersistenceService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final ExtractionMapper extractionMapper;
    private final ObjectMapper objectMapper;

    public PersistenceServiceImpl(
            ConversationMapper conversationMapper,
            MessageMapper messageMapper,
            ExtractionMapper extractionMapper,
            ObjectMapper objectMapper) {
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
        this.extractionMapper = extractionMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 加载会话状态
     * 从多个规范化表中组装 DeepTalentState 对象
     */
    @Override
    @Transactional(readOnly = true)
    public DeepTalentState loadState(String threadId) {
        // 1. 查询会话基础信息
        ConversationEntity convEntity = conversationMapper.selectById(threadId);
        if (convEntity == null) {
            return new DeepTalentState();
        }

        DeepTalentState state = new DeepTalentState();
        
        // 2. 还原基础属性
        state.setCurrentPhase(Phase.valueOf(convEntity.getCurrentPhase().toUpperCase()));
        state.setDialogueCount(convEntity.getDialogueCount() != null ? convEntity.getDialogueCount() : 0);
        state.setNeedFollowup(Boolean.TRUE.equals(convEntity.getNeedFollowup()));
        state.setFinalReport(convEntity.getFinalReport());

        // 3. 查询并组装消息列表
        List<MessageEntity> msgEntities = messageMapper.selectList(
                new LambdaQueryWrapper<MessageEntity>()
                        .eq(MessageEntity::getThreadId, threadId)
                        .orderByAsc(MessageEntity::getSequence)
        );
        List<Message> messages = msgEntities.stream()
                .map(e -> new Message(e.getRole(), e.getContent()))
                .collect(Collectors.toList());
        state.setMessages(messages);

        // 4. 查询并组装用户画像 (Extractions)
        List<ExtractionEntity> extEntities = extractionMapper.selectList(
                new LambdaQueryWrapper<ExtractionEntity>()
                        .eq(ExtractionEntity::getThreadId, threadId)
        );
        
        Map<String, List<Extraction>> userProfile = new HashMap<>();
        for (ExtractionEntity ext : extEntities) {
            String phase = ext.getPhase();
            Extraction extraction = new Extraction(ext.getTag(), ext.getEvidence(), phase, ext.getConfidence());
            userProfile.computeIfAbsent(phase, k -> new ArrayList<>()).add(extraction);
        }
        state.setUserProfile(userProfile);

        return state;
    }

    /**
     * 保存会话状态
     * 将 DeepTalentState 拆解并保存到多个表中
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveState(String threadId, DeepTalentState state) {
        // 1. 保存或更新会话基础信息
        ConversationEntity convEntity = conversationMapper.selectById(threadId);
        boolean isNew = (convEntity == null);
        
        if (isNew) {
            convEntity = new ConversationEntity();
            convEntity.setThreadId(threadId);
            convEntity.setCreatedAt(LocalDateTime.now());
        }
        
        convEntity.setCurrentPhase(state.getCurrentPhase().getValue());
        convEntity.setDialogueCount(state.getDialogueCount());
        convEntity.setNeedFollowup(state.isNeedFollowup());
        convEntity.setFinalReport(state.getFinalReport());
        convEntity.setUpdatedAt(LocalDateTime.now());
        
        if (isNew) {
            conversationMapper.insert(convEntity);
        } else {
            conversationMapper.updateById(convEntity);
        }

        // 2. 全量替换消息列表 (简化处理，实际生产可做增量)
        // 删除旧消息
        messageMapper.delete(new LambdaQueryWrapper<MessageEntity>().eq(MessageEntity::getThreadId, threadId));
        // 插入新消息
        List<Message> messages = state.getMessages();
        if (messages != null) {
            for (int i = 0; i < messages.size(); i++) {
                Message msg = messages.get(i);
                MessageEntity msgEntity = new MessageEntity();
                msgEntity.setThreadId(threadId);
                msgEntity.setRole(msg.getRole());
                msgEntity.setContent(msg.getContent());
                msgEntity.setSequence(i);
                msgEntity.setCreatedAt(LocalDateTime.now());
                messageMapper.insert(msgEntity);
            }
        }

        // 3. 全量替换特征提取记录
        // 删除旧特征
        extractionMapper.delete(new LambdaQueryWrapper<ExtractionEntity>().eq(ExtractionEntity::getThreadId, threadId));
        // 插入新特征
        Map<String, List<Extraction>> profile = state.getUserProfile();
        if (profile != null) {
            profile.forEach((phase, extractions) -> {
                if (extractions != null) {
                    for (Extraction ext : extractions) {
                        ExtractionEntity extEntity = new ExtractionEntity();
                        extEntity.setThreadId(threadId);
                        extEntity.setPhase(phase);
                        extEntity.setTag(ext.getTag());
                        extEntity.setEvidence(ext.getEvidence());
                        extEntity.setConfidence(ext.getConfidence());
                        extEntity.setCreatedAt(LocalDateTime.now());
                        extractionMapper.insert(extEntity);
                    }
                }
            });
        }
    }
}
