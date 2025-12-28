package com.deeptalent.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 深度天赋分析状态类
 * 维护用户会话过程中的所有状态信息，包括对话历史、当前阶段、用户画像等
 *
 * @author 小阳
 * @date 2025-12-28
 * @version 1.0.0
 */
public class DeepTalentState {
    /**
     * 对话消息历史列表
     * 记录系统、用户和助手之间的所有交互消息
     */
    private List<Message> messages = new ArrayList<>();

    /**
     * 当前对话阶段
     * 默认为童年阶段(CHILDHOOD)，随着对话进行会流转到不同阶段
     */
    private Phase currentPhase = Phase.CHILDHOOD;
    
    /**
     * 用户画像数据
     * 键为阶段名称，值为该阶段提取出的特征信息列表
     * Map<PhaseName, List<Extraction>>
     */
    private Map<String, List<Extraction>> userProfile = new HashMap<>();
    
    /**
     * 对话轮数计数器
     * 记录当前进行的对话交互次数
     */
    private int dialogueCount = 0;

    /**
     * 是否需要追问标志
     * 标识当前是否需要对用户的回答进行进一步追问
     */
    private boolean needFollowup = false;

    /**
     * 最近一次评估结果
     * 存储上一次对话评估的详细结果
     */
    private EvaluationResult lastEval;

    /**
     * 最终分析报告
     * 整个咨询过程结束后生成的完整报告内容
     */
    private String finalReport;

    public DeepTalentState() {
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Phase getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(Phase currentPhase) {
        this.currentPhase = currentPhase;
    }

    public Map<String, List<Extraction>> getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(Map<String, List<Extraction>> userProfile) {
        this.userProfile = userProfile;
    }

    public int getDialogueCount() {
        return dialogueCount;
    }

    public void setDialogueCount(int dialogueCount) {
        this.dialogueCount = dialogueCount;
    }

    public boolean isNeedFollowup() {
        return needFollowup;
    }

    public void setNeedFollowup(boolean needFollowup) {
        this.needFollowup = needFollowup;
    }

    public EvaluationResult getLastEval() {
        return lastEval;
    }

    public void setLastEval(EvaluationResult lastEval) {
        this.lastEval = lastEval;
    }

    public String getFinalReport() {
        return finalReport;
    }

    public void setFinalReport(String finalReport) {
        this.finalReport = finalReport;
    }
}
