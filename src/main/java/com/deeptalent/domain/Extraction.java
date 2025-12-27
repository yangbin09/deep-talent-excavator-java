package com.deeptalent.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 特征提取实体类
 * 用于描述从用户对话中提取出的具体特征、证据及置信度
 *
 * @author 小阳
 * @date 2025-12-27
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Extraction {
    /**
     * 标签
     * 提取出的特征标签或关键词
     */
    private String tag;

    /**
     * 证据
     * 支持该特征标签的对话原文或事实依据
     */
    private String evidence;

    /**
     * 阶段
     * 该特征所属的对话阶段
     */
    private String phase;

    /**
     * 置信度
     * 算法对该提取结果准确性的置信度评分
     */
    private double confidence;
}
