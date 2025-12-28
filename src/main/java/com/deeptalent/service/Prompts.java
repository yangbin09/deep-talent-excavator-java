package com.deeptalent.service;

/**
 * 提示词常量类
 * 仅定义提示词 Key，具体内容从数据库动态加载
 *
 * @author 小阳
 * @date 2025-12-28
 * @version 2.0.0
 */
public class Prompts {

    /**
     * 评估与信息抽取助手提示词 Key
     */
    public static final String EVALUATOR_SYSTEM_PROMPT = "EVALUATOR_SYSTEM_PROMPT";

    /**
     * 访谈主持人提示词 Key
     */
    public static final String INTERVIEWER_SYSTEM_PROMPT = "INTERVIEWER_SYSTEM_PROMPT";

    /**
     * 报告生成分析师提示词 Key
     */
    public static final String WRITER_SYSTEM_PROMPT = "WRITER_SYSTEM_PROMPT";
}
