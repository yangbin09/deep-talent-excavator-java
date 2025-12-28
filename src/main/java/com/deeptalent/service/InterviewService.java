package com.deeptalent.service;

/**
 * 面试服务接口
 * 定义面试流程的核心业务操作，包括会话的启动、恢复和消息处理
 *
 * @author 小阳
 * @date 2025-12-28
 * @version 1.0.0
 */
public interface InterviewService {

    /**
     * 启动或恢复面试会话
     *
     * @param threadId 会话唯一标识符
     * @return AI 的欢迎语或上一条消息
     */
    String startSession(String threadId);

    /**
     * 处理用户消息并推进对话流程
     *
     * @param threadId           会话唯一标识符
     * @param userMessageContent 用户输入的消息内容
     * @return AI 的回复内容
     */
    String chat(String threadId, String userMessageContent);
}
