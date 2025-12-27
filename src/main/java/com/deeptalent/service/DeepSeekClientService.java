package com.deeptalent.service;

import com.deeptalent.domain.Message;
import java.util.List;

/**
 * DeepSeek API 客户端服务接口
 * 定义与 DeepSeek 大模型交互的标准操作
 */
public interface DeepSeekClientService {

    /**
     * 发送聊天请求到 DeepSeek API
     *
     * @param messages    对话历史消息列表
     * @param temperature 生成温度（创造性程度，0.0-1.0）
     * @param jsonMode    是否强制返回 JSON 格式
     * @return 模型生成的回复内容
     */
    String chat(List<Message> messages, double temperature, boolean jsonMode);
}
