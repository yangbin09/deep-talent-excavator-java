package com.deeptalent.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提示词配置实体类
 * 对应数据库 dt_prompt_config 表，用于存储动态提示词配置
 *
 * @author 小阳
 * @date 2025-12-28
 * @version 1.0.0
 */
@Data
@TableName("dt_prompt_config")
public class PromptConfigEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 提示词Key (唯一标识)
     */
    private String promptKey;

    /**
     * 语言代码 (默认 zh-CN)
     */
    private String language;

    /**
     * 提示词内容模板
     */
    private String content;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 是否为当前生效版本
     */
    @TableField("is_active")
    private Boolean active;

    /**
     * 描述说明
     */
    private String description;

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

    public PromptConfigEntity() {
    }


}
