package com.deeptalent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息记录实体类
 * 存储具体的对话内容
 *
 * @author 小阳
 * @date 2025-12-28
 * @version 1.0.0
 */
@Data
@TableName("dt_message")
public class MessageEntity {
    
    /**
     * 消息ID (自增主键)
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属会话ID (外键)
     */
    private String threadId;

    /**
     * 消息角色 (user/assistant/system)
     */
    private String role;

    /**
     * 消息内容 (长文本)
     */
    private String content;

    /**
     * 消息顺序 (用于排序)
     */
    private Integer sequence;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public MessageEntity() {
    }

    public MessageEntity(Long id, String threadId, String role, String content, Integer sequence, LocalDateTime createdAt) {
        this.id = id;
        this.threadId = threadId;
        this.role = role;
        this.content = content;
        this.sequence = sequence;
        this.createdAt = createdAt;
    }


}
