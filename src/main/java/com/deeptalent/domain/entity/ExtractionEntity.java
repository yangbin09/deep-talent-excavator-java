package com.deeptalent.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 特征提取记录实体类
 * 存储从用户对话中提取的画像特征
 *
 * @author 小阳
 * @date 2025-12-28
 * @version 1.0.0
 */
@Data
@TableName("dt_extraction")
public class ExtractionEntity {

    /**
     * ID (自增主键)
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属会话ID (外键)
     */
    private String threadId;

    /**
     * 所属阶段 (Phase)
     */
    private String phase;

    /**
     * 特征标签
     */
    private String tag;

    /**
     * 证据文本
     */
    private String evidence;

    /**
     * 置信度
     */
    private Double confidence;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public ExtractionEntity() {
    }

    public ExtractionEntity(Long id, String threadId, String phase, String tag, String evidence, Double confidence, LocalDateTime createdAt) {
        this.id = id;
        this.threadId = threadId;
        this.phase = phase;
        this.tag = tag;
        this.evidence = evidence;
        this.confidence = confidence;
        this.createdAt = createdAt;
    }


}
