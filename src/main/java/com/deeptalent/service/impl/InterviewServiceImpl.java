package com.deeptalent.service.impl;

import com.deeptalent.domain.ai.Message;
import com.deeptalent.domain.enums.Phase;
import com.deeptalent.domain.model.DeepTalentState;
import com.deeptalent.domain.model.EvaluationResult;
import com.deeptalent.domain.model.Extraction;
import com.deeptalent.service.InterviewService;
import com.deeptalent.service.PersistenceService;
import com.deeptalent.service.PromptService;
import com.deeptalent.service.Prompts;
import com.deeptalent.service.ai.DeepTalentAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 访谈服务实现类
 *
 * @author 小阳
 * @date 2025-12-28
 * @version 1.0.0
 */
@Service
public class InterviewServiceImpl implements InterviewService {

    private static final Logger log = LoggerFactory.getLogger(InterviewServiceImpl.class);

    private final DeepTalentAgent deepTalentAgent;
    private final PersistenceService persistenceService;
    private final PromptService promptService;

    /**
     * 构造函数
     *
     * @param deepTalentAgent    LangChain4j AI Agent
     * @param persistenceService 持久化服务
     * @param promptService      提示词服务
     */
    public InterviewServiceImpl(DeepTalentAgent deepTalentAgent, PersistenceService persistenceService, PromptService promptService) {
        this.deepTalentAgent = deepTalentAgent;
        this.persistenceService = persistenceService;
        this.promptService = promptService;
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
        String promptTemplate = promptService.getPrompt(Prompts.INTERVIEWER_SYSTEM_PROMPT);
        String prompt = promptTemplate
                .replace("{phase}", currentPhase.getValue())
                .replace("{dialogue_count}", String.valueOf(newCount))
                .replace("{last_eval}", lastEval != null ? lastEval.toString() : "无"); 
        
        // 构建上下文：历史消息 (将 System Prompt 加入到消息列表头部)
        List<dev.langchain4j.data.message.ChatMessage> history = convertMessages(state.getMessages());
        history.add(0, dev.langchain4j.data.message.SystemMessage.from(prompt));
        
        // 调用 LLM 生成问题
        String response = deepTalentAgent.chat(history);
        
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
        
        try {
            // 获取并处理 Prompt
            String promptTemplate = promptService.getPrompt(Prompts.EVALUATOR_SYSTEM_PROMPT);
            String systemPrompt = promptTemplate.replace("{phase}", currentPhase.getValue());

            // 调用 LLM (LangChain4j 自动处理结构化输出)
            List<dev.langchain4j.data.message.ChatMessage> evalMessages = new ArrayList<>();
            evalMessages.add(dev.langchain4j.data.message.SystemMessage.from(systemPrompt));
            evalMessages.add(dev.langchain4j.data.message.UserMessage.from(lastUserContent));
            
            EvaluationResult result = deepTalentAgent.evaluate(evalMessages);
            
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
        String promptTemplate = promptService.getPrompt(Prompts.WRITER_SYSTEM_PROMPT);
        String prompt = promptTemplate.replace("{user_profile}", String.valueOf(profile));
        
        // 构建上下文：历史消息
        List<dev.langchain4j.data.message.ChatMessage> history = convertMessages(state.getMessages());
        history.add(0, dev.langchain4j.data.message.SystemMessage.from(prompt));
        
        // 调用 LLM 生成报告
        String response = deepTalentAgent.chat(history);
        
        // 保存报告
        state.setFinalReport(response);
        state.getMessages().add(new Message("assistant", response));
        
        return state;
    }
    
    /**
     * 将业务消息转换为 LangChain4j 消息
     */
    private List<dev.langchain4j.data.message.ChatMessage> convertMessages(List<Message> messages) {
        List<dev.langchain4j.data.message.ChatMessage> chatMessages = new ArrayList<>();
        for (Message msg : messages) {
            if ("user".equals(msg.getRole())) {
                chatMessages.add(new dev.langchain4j.data.message.UserMessage(msg.getContent()));
            } else if ("assistant".equals(msg.getRole())) {
                chatMessages.add(new dev.langchain4j.data.message.AiMessage(msg.getContent()));
            } 
            // 注意：我们不将 system 消息添加到历史中，因为我们在调用 chat 时会单独传递 system prompt
        }
        return chatMessages;
    }
}
