package com.deeptalent.controller;

import com.deeptalent.service.InterviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 面试控制器类，处理与面试对话相关的HTTP请求
 * 提供面试会话的启动和消息交互功能
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*") // 允许前端跨域访问
public class InterviewController {

    /**
     * 面试服务实例，处理面试相关业务逻辑
     */
    private final InterviewService interviewService;

    /**
     * 构造函数，通过依赖注入初始化InterviewService
     * @param interviewService 面试服务实例
     */
    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    /**
     * 启动新的面试会话
     * @param threadId 会话唯一标识符
     * @return 包含threadId和欢迎消息的响应实体
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startSession(@RequestParam String threadId) {
        // 调用服务层启动会话
        String response = interviewService.startSession(threadId);
        // 返回包含会话ID和响应消息的JSON格式数据
        return ResponseEntity.ok(Map.of("threadId", threadId, "message", response));
    }

    /**
     * 发送消息到面试会话并获取回复
     * @param payload 包含threadId和message的请求体
     * @return 包含threadId和AI回复的响应实体
     */
    @PostMapping("/message")
    public ResponseEntity<Map<String, String>> sendMessage(@RequestBody Map<String, String> payload) {
        // 从请求体中提取会话ID和消息内容
        String threadId = payload.get("threadId");
        String message = payload.get("message");
        
        // 参数校验：确保必要参数不为空
        if (threadId == null || message == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // 调用服务层处理对话
        String response = interviewService.chat(threadId, message);
        // 返回包含会话ID和AI回复的JSON格式数据
        return ResponseEntity.ok(Map.of("threadId", threadId, "message", response));
    }
}