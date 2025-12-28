package com.deeptalent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.deeptalent.domain.entity.PromptConfigEntity;
import com.deeptalent.mapper.PromptConfigMapper;
import com.deeptalent.service.PromptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 提示词服务实现类
 * 实现提示词的数据库加载、内存缓存及定期刷新
 *
 * @author 小阳
 * @date 2025-12-28
 * @version 1.0.0
 */
@Service
public class PromptServiceImpl implements PromptService {

    private static final Logger log = LoggerFactory.getLogger(PromptServiceImpl.class);
    
    // 内存缓存: key -> content (Key 格式: promptKey_language)
    private final Map<String, String> promptCache = new ConcurrentHashMap<>();
    
    private final PromptConfigMapper promptConfigMapper;

    public PromptServiceImpl(PromptConfigMapper promptConfigMapper) {
        this.promptConfigMapper = promptConfigMapper;
    }

    /**
     * 应用启动时初始化加载缓存
     */
    @PostConstruct
    public void init() {
        log.info("Initializing Prompt Cache...");
        refreshCache();
    }

    /**
     * 每 5 分钟刷新一次缓存 (300000 ms)
     * 确保数据一致性
     */
    @Scheduled(fixedRate = 300000)
    public void scheduledRefresh() {
        log.info("Scheduled refreshing Prompt Cache...");
        refreshCache();
    }

    @Override
    public String getPrompt(String key) {
        return getPrompt(key, "zh-CN");
    }

    @Override
    public String getPrompt(String key, String language) {
        String cacheKey = buildCacheKey(key, language);
        String content = promptCache.get(cacheKey);
        
        if (content == null) {
            log.warn("Prompt not found in cache for key: {}, lang: {}. Trying database fallback.", key, language);
            // 缓存未命中（理论上初始化后不应发生，除非是新加的），尝试查库并更新缓存
            content = loadFromDatabase(key, language);
            if (content != null) {
                promptCache.put(cacheKey, content);
            } else {
                log.error("Prompt not found in database for key: {}, lang: {}", key, language);
                throw new RuntimeException("Prompt not found: " + key);
            }
        }
        return content;
    }

    @Override
    public synchronized void refreshCache() {
        try {
            // 查询所有激活的配置
            List<PromptConfigEntity> configs = promptConfigMapper.selectList(
                new LambdaQueryWrapper<PromptConfigEntity>()
                    .eq(PromptConfigEntity::getActive, true)
            );

            if (configs.isEmpty()) {
                log.warn("No active prompts found in database.");
                return;
            }

            // 更新缓存
            for (PromptConfigEntity config : configs) {
                String cacheKey = buildCacheKey(config.getPromptKey(), config.getLanguage());
                promptCache.put(cacheKey, config.getContent());
            }
            
            log.info("Prompt Cache refreshed. Total items: {}", promptCache.size());
        } catch (Exception e) {
            log.error("Failed to refresh Prompt Cache", e);
        }
    }
    
    private String loadFromDatabase(String key, String language) {
        PromptConfigEntity entity = promptConfigMapper.selectOne(
            new LambdaQueryWrapper<PromptConfigEntity>()
                .eq(PromptConfigEntity::getPromptKey, key)
                .eq(PromptConfigEntity::getLanguage, language)
                .eq(PromptConfigEntity::getActive, true)
        );
        return entity != null ? entity.getContent() : null;
    }

    private String buildCacheKey(String key, String language) {
        return key + "_" + language;
    }
}
