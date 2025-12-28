package com.deeptalent.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 评估结果实体类
 * 用于封装对用户回答的评估分析结果，包括评分、追问建议及特征提取
 *
 * @author 小阳
 * @date 2025-12-28
 * @version 1.0.0
 */
@Setter
@Getter
public class EvaluationResult {
    /**
     * 评分
     * 对用户回答质量或相关性的量化评分
     */
    private int score;
    
    /**
     * 是否需要追问
     * 标识是否需要根据用户的回答进行进一步的询问
     */
    @JsonProperty("need_followup")
    private boolean needFollowup;
    
    /**
     * 追问问题内容
     * 如果需要追问，此处存储具体的追问问题文本
     */
    @JsonProperty("followup_question")
    private String followupQuestion;
    
    /**
     * 提取的特征列表
     * 从用户回答中提取出的关键信息或特征点
     */
    private List<Extraction> extractions;

    /**
     * 评估理由
     * 给出评分或追问建议的具体原因说明
     */
    private String reason;

    public EvaluationResult() {
    }

    @Override
    public String toString() {
        return "EvaluationResult{" +
                "score=" + score +
                ", needFollowup=" + needFollowup +
                ", followupQuestion='" + followupQuestion + '\'' +
                ", extractions=" + extractions +
                ", reason='" + reason + '\'' +
                '}';
    }
}
