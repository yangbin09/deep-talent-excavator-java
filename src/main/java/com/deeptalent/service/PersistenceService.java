package com.deeptalent.service;

import com.deeptalent.domain.DeepTalentState;

/**
 * 持久化服务接口
 * 定义会话状态的保存与加载操作，负责将内存中的状态持久化到数据库
 *
 * @author 小阳
 * @date 2025-12-28
 * @version 1.0.0
 */
public interface PersistenceService {

    /**
     * 加载会话状态
     *
     * @param threadId 会话唯一标识符
     * @return 加载的状态对象
     */
    DeepTalentState loadState(String threadId);

    /**
     * 保存会话状态
     *
     * @param threadId 会话唯一标识符
     * @param state    要保存的状态对象
     */
    void saveState(String threadId, DeepTalentState state);
}
