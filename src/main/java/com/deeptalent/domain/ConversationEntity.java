package com.deeptalent.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 此时会话实体类
 * 用于持久化存储用户会话状态及相关数据
 *
 * @author 小阳
 * @date 2025-12-27
 * @version 1.0.0
 */
@Entity
@Table(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationEntity {
    
    /**
     * 会话唯一标识符
     * 通常使用UUID或其他唯一字符串作为主键
     */
    @Id
    private String threadId;
    
    /**
     * 会话状态的JSON字符串表示
     * 存储DeepTalentState对象的序列化数据，用于恢复会话上下文
     */
    @Column(columnDefinition = "TEXT")
    private String stateJson;
}
