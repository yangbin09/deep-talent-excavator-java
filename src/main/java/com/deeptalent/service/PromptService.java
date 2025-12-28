package com.deeptalent.service;

/**
 * 提示词服务接口
 * 提供提示词的动态加载、缓存管理和刷新功能
 *
 * @author 小阳
 * @date 2025-12-28
 * @version 1.0.0
 */
public interface PromptService {

    /**
     * 获取指定 Key 的提示词内容（默认语言 zh-CN）
     *
     * @param key 提示词 Key
     * @return 提示词内容模板
     */
    String getPrompt(String key);

    /**
     * 获取指定 Key 和语言的提示词内容
     *
     * @param key 提示词 Key
     * @param language 语言代码
     * @return 提示词内容模板
     */
    String getPrompt(String key, String language);

    /**
     * 强制刷新提示词缓存
     */
    void refreshCache();
}
