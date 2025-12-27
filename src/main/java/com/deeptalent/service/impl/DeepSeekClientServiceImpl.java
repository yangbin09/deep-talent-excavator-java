package com.deeptalent.service.impl;

import com.deeptalent.domain.Message;
import com.deeptalent.service.DeepSeekClientService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * DeepSeek API 客户端服务实现类
 * 负责与 DeepSeek 大模型 API 进行通信
 */
@Service
public class DeepSeekClientServiceImpl implements DeepSeekClientService {

    private final RestClient restClient;
    private final String chatModel;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数，初始化 RestClient 和配置
     *
     * @param baseUrl   DeepSeek API 基础路径
     * @param apiKey    API 密钥
     * @param chatModel 使用的模型名称
     * @param objectMapper Jackson ObjectMapper 实例
     */
    public DeepSeekClientServiceImpl(
            @Value("${deepseek.base-url}") String baseUrl,
            @Value("${deepseek.api-key}") String apiKey,
            @Value("${deepseek.chat-model}") String chatModel,
            ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
        // 初始化 RestClient，设置 Base URL 和默认 Authorization 头
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    /**
     * 发送聊天请求到 DeepSeek API
     *
     * @param messages    对话历史消息列表
     * @param temperature 生成温度（创造性程度）
     * @param jsonMode    是否强制返回 JSON 格式
     * @return 模型生成的回复内容
     */
    @Override
    public String chat(List<Message> messages, double temperature, boolean jsonMode) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("model", chatModel);
            requestBody.put("messages", messages);
            // JSON 模式下建议 temperature 设置为 0 以保证稳定性
            requestBody.put("temperature", jsonMode ? 0 : temperature);
            if (jsonMode) {
                requestBody.put("response_format", Map.of("type", "json_object"));
            }

            // 发起 POST 请求
            String response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            // 解析响应 JSON，提取 content 字段
            JsonNode root = objectMapper.readTree(response);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            // 异常处理：包装为 RuntimeException 抛出
            throw new RuntimeException("DeepSeek API Call Failed: " + e.getMessage(), e);
        }
    }
}
