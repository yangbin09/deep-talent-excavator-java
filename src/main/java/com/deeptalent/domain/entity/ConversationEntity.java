package com.deeptalent.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 会话基础实体类
 * 
 * 重构说明：
 * 1. 拆分原 stateJson 中的结构化字段
 * 2. 增加审计字段（创建时间、更新时间）
 * 3. 规范化命名
 *
 * @author 小阳
 * @date 2025-12-28
 * @version 2.0.0
 */
@Setter
@Getter
@TableName("dt_conversation")
public class ConversationEntity {
    
    /**
     * 会话唯一标识符 (主键)
     */
    @TableId
    private String threadId;
    
    /**
     * 当前对话阶段
     * 对应 Phase 枚举
     */
    private String currentPhase;
    
    /**
     * 对话轮数计数器
     */
    private Integer dialogueCount;
    
    /**
     * 是否需要追问
     */
    private Boolean needFollowup;
    
    /**
     * 最终分析报告 (长文本)
     */
    private String finalReport;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;


    public ConversationEntity() {
    }

}
