package com.deeptalent.service.impl;

import com.deeptalent.domain.*;
import com.deeptalent.service.DeepSeekClientService;
import com.deeptalent.service.InterviewService;
import com.deeptalent.service.PersistenceService;
import com.deeptalent.service.Prompts;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 面试服务实现类
 * 核心业务逻辑层，管理面试流程、状态流转和各个节点的执行
 */
@Service
@Slf4j
public class InterviewServiceImpl implements InterviewService {

    private final DeepSeekClientService llmClient;
    private final PersistenceService persistenceService;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数
     *
     * @param llmClient          LLM 客户端服务
     * @param persistenceService 持久化服务
     * @param objectMapper       JSON 工具
     */
    public InterviewServiceImpl(DeepSeekClientService llmClient, PersistenceService persistenceService, ObjectMapper objectMapper) {
        this.llmClient = llmClient;
        this.persistenceService = persistenceService;
        this.objectMapper = objectMapper;
    }

    /**
     * 启动或恢复面试会话
     *
     * @param threadId 会话唯一标识符
     * @return AI 的欢迎语或上一条消息
     */
    @Override
    public String startSession(String threadId) {
        // 1. 加载状态
        DeepTalentState state = persistenceService.loadState(threadId);
        
        // 2. 判断是否是新会话（无消息记录）
        if (state.getMessages().isEmpty()) {
            // 冷启动：直接进入访谈节点生成第一个问题
            state = interviewerNode(state);
            // 保存初始状态
            persistenceService.saveState(threadId, state);
            return getLastAssistantMessage(state);
        }
        
        // 3. 如果是恢复会话，直接返回最后一条助手消息
        return getLastAssistantMessage(state);
    }

    /**
     * 处理用户消息并推进对话流程
     *
     * @param threadId           会话唯一标识符
     * @param userMessageContent 用户输入的消息内容
     * @return AI 的回复内容
     */
    @Override
    public String chat(String threadId, String userMessageContent) {
        // 1. 加载当前状态
        DeepTalentState state = persistenceService.loadState(threadId);
        
        // 2. 添加用户消息到历史记录
        state.getMessages().add(new Message("user", userMessageContent));
        
        // 3. 执行评估节点 (Evaluator Node) - 分析用户回答质量并提取信息
        state = evaluatorNode(state);
        
        // 4. 执行路由节点 (Router Node) - 决定下一步流程
        String nextStep = routerNode(state);
        
        // 5. 根据路由结果执行相应节点
        if ("writer".equals(nextStep)) {
            // 生成报告阶段
            state = writerNode(state);
        } else {
            // 继续访谈阶段
            state = interviewerNode(state);
        }
        
        // 6. 保存更新后的状态
        persistenceService.saveState(threadId, state);
        
        // 7. 如果生成了最终报告，优先返回报告内容
        if (state.getFinalReport() != null && "writer".equals(nextStep)) {
            return state.getFinalReport();
        }
        
        // 8. 返回助手的最新回复
        return getLastAssistantMessage(state);
    }
    
    /**
     * 获取对话历史中最后一条助手消息
     *
     * @param state 当前状态
     * @return 消息内容，若无则返回空字符串
     */
    private String getLastAssistantMessage(DeepTalentState state) {
        List<Message> msgs = state.getMessages();
        if (msgs.isEmpty()) return "";
        Message last = msgs.get(msgs.size() - 1);
        if ("assistant".equals(last.getRole())) {
            return last.getContent();
        }
        return "";
    }

    // --- 内部核心节点逻辑 ---

    /**
     * 访谈节点：生成下一个面试问题
     */
    private DeepTalentState interviewerNode(DeepTalentState state) {
        Phase currentPhase = state.getCurrentPhase();
        int dialogueCount = state.getDialogueCount();
        EvaluationResult lastEval = state.getLastEval();
        
        // 对话轮次 + 1
        int newCount = dialogueCount + 1;
        
        // 填充 Prompt 模板
        String prompt = Prompts.INTERVIEWER_SYSTEM_PROMPT
                .replace("{phase}", currentPhase.getValue())
                .replace("{dialogue_count}", String.valueOf(newCount))
                .replace("{last_eval}", String.valueOf(lastEval)); 
        
        // 构建上下文：System Prompt + 历史消息
        List<Message> context = new ArrayList<>();
        context.add(new Message("system", prompt));
        context.addAll(state.getMessages());
        
        // 调用 LLM 生成问题
        String response = llmClient.chat(context, 0.7, false);
        
        // 更新状态
        state.getMessages().add(new Message("assistant", response));
        state.setDialogueCount(newCount);
        
        return state;
    }

    /**
     * 评估节点：分析用户回答，提取信息，判断是否需要追问
     */
    private DeepTalentState evaluatorNode(DeepTalentState state) {
        List<Message> messages = state.getMessages();
        Phase currentPhase = state.getCurrentPhase();
        
        // 如果没有消息或最后一条不是用户的（理论上不会发生），直接返回
        if (messages.isEmpty() || !"user".equals(messages.get(messages.size() - 1).getRole())) {
            return state;
        }
        
        String lastUserContent = messages.get(messages.size() - 1).getContent();
        
        // 填充评估 Prompt
        String prompt = Prompts.EVALUATOR_SYSTEM_PROMPT.replace("{phase}", currentPhase.getValue());
        
        // 构建上下文：System Prompt + 用户最新一条回答
        List<Message> context = new ArrayList<>();
        context.add(new Message("system", prompt));
        context.add(new Message("user", lastUserContent));
        
        try {
            // 调用 LLM (JSON 模式)
            String jsonResponse = llmClient.chat(context, 0, true);
            // 解析 JSON 结果
            EvaluationResult result = objectMapper.readValue(jsonResponse, EvaluationResult.class);
            
            // 更新画像信息 (Extractions)
            Map<String, List<Extraction>> profile = state.getUserProfile();
            List<Extraction> phaseExtractions = profile.getOrDefault(currentPhase.getValue(), new ArrayList<>());
            if (result.getExtractions() != null) {
                phaseExtractions.addAll(result.getExtractions());
            }
            profile.put(currentPhase.getValue(), phaseExtractions);
            
            // 更新状态标记
            state.setNeedFollowup(result.isNeedFollowup());
            state.setLastEval(result);
            state.setUserProfile(profile);
            
        } catch (Exception e) {
            log.error("Evaluator failed", e);
            // 降级策略：如果解析失败，默认进行追问
            state.setNeedFollowup(true);
            EvaluationResult fallback = new EvaluationResult();
            fallback.setReason("解析失败: " + e.getMessage());
            fallback.setScore(0);
            fallback.setFollowupQuestion("你可以再具体说一点吗？比如当时发生了什么、你有什么感受？");
            state.setLastEval(fallback);
        }
        
        return state;
    }

    /**
     * 路由节点：决定流程走向（继续访谈 / 切换阶段 / 生成报告）
     */
    private String routerNode(DeepTalentState state) {
        // 1. 如果评估结果认为需要追问，保持在访谈节点
        if (state.isNeedFollowup()) {
            return "interviewer";
        }
        
        // 2. 如果已经在生成报告阶段，直接去 writer
        if (state.getCurrentPhase() == Phase.GENERATING) {
            return "writer";
        }
        
        // 3. 判断是否满足阶段切换条件
        Phase currentPhase = state.getCurrentPhase();
        int dialogueCount = state.getDialogueCount();
        Map<String, List<Extraction>> profile = state.getUserProfile();
        List<Extraction> extractions = profile.getOrDefault(currentPhase.getValue(), new ArrayList<>());
        
        // 阶段切换阈值
        int MAX_TURNS = 4;        // 最大对话轮次
        int MIN_EXTRACTIONS = 2;  // 最小提取信息点数
        
        boolean shouldAdvance = false;
        if (dialogueCount >= MAX_TURNS) {
            shouldAdvance = true;
        } else if (extractions.size() >= MIN_EXTRACTIONS) {
            shouldAdvance = true;
        }
        
        if (shouldAdvance) {
            // 确定下一阶段
            Phase nextPhase = null;
            if (currentPhase == Phase.CHILDHOOD) nextPhase = Phase.COMPETENCE;
            else if (currentPhase == Phase.COMPETENCE) nextPhase = Phase.SHADOW;
            else if (currentPhase == Phase.SHADOW) nextPhase = Phase.GENERATING;
            
            if (nextPhase != null) {
                // 执行切换
                state.setCurrentPhase(nextPhase);
                state.setDialogueCount(0);
                state.setNeedFollowup(false);
                
                if (nextPhase == Phase.GENERATING) {
                    return "writer";
                }
            }
        }
        
        return "interviewer";
    }

    /**
     * 写作节点：生成最终天赋画像报告
     */
    private DeepTalentState writerNode(DeepTalentState state) {
        Map<String, List<Extraction>> profile = state.getUserProfile();
        
        // 填充 Prompt
        String prompt = Prompts.WRITER_SYSTEM_PROMPT.replace("{user_profile}", String.valueOf(profile));
        
        // 构建上下文
        List<Message> context = new ArrayList<>();
        context.add(new Message("system", prompt));
        context.addAll(state.getMessages());
        
        // 调用 LLM 生成报告
        String response = llmClient.chat(context, 0.7, false);
        
        // 保存报告
        state.setFinalReport(response);
        state.getMessages().add(new Message("assistant", response));
        
        return state;
    }
}
