package com.deeptalent.service.ai;

import com.deeptalent.domain.EvaluationResult;
import dev.langchain4j.service.spring.AiService;

import dev.langchain4j.data.message.ChatMessage;
import java.util.List;

/**
 * 深度天赋挖掘 AI 代理接口
 * 基于 LangChain4j 的声明式 AI 服务，定义了与大模型交互的接口
 *
 * @author 小阳
 * @date 2025-12-28
 * @version 1.0.0
 */
@AiService
public interface DeepTalentAgent {

    String chat(List<ChatMessage> messages);

    EvaluationResult evaluate(List<ChatMessage> messages);
}
