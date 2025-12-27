package com.deeptalent.domain;

/**
 * 对话阶段枚举类
 * 定义咨询过程中的不同阶段，用于控制对话流程
 *
 * @author 小阳
 * @date 2025-12-27
 * @version 1.0.0
 */
public enum Phase {
    /**
     * 童年阶段
     * 探索用户的童年经历和早期记忆
     */
    CHILDHOOD("childhood"),

    /**
     * 胜任力阶段
     * 分析用户的核心能力和优势领域
     */
    COMPETENCE("competence"),

    /**
     * 阴影阶段
     * 探索用户的潜在心理阴影或挑战
     */
    SHADOW("shadow"),

    /**
     * 生成报告阶段
     * 汇总所有信息并生成最终分析报告
     */
    GENERATING("generating");

    /**
     * 阶段的字符串值
     */
    private final String value;

    /**
     * 构造函数
     *
     * @param value 阶段对应的字符串标识
     */
    Phase(String value) {
        this.value = value;
    }

    /**
     * 获取阶段的字符串值
     *
     * @return 阶段字符串
     */
    public String getValue() {
        return value;
    }
}
