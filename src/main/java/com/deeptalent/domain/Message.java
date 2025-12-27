package com.deeptalent.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 消息实体类
 * 表示对话中的单条消息记录，包含角色和内容
 *
 * @author 小阳
 * @date 2025-12-27
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
