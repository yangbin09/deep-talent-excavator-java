package com.deeptalent.domain.ai;

import lombok.Getter;
import lombok.Setter;

/**
 * 消息实体类
 * 表示对话中的单条消息记录，包含角色和内容
 *
 * @author 小阳
 * @date 2025-12-28
 * @version 1.0.0
 */
@Setter
@Getter
public class Message {
    /**
     * 消息角色
     * 标识消息的发送者，通常为 "user" (用户)、"assistant" (助手) 或 "system" (系统)
     */
    private String role; // "user" or "assistant" or "system"

    /**
     * 消息内容
     * 具体的文本内容
     */
    private String content;

    public Message() {
    }

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

}
