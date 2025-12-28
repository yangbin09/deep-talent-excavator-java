package com.deeptalent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.deeptalent.domain.entity.PromptConfigEntity;
import com.deeptalent.mapper.PromptConfigMapper;
import com.deeptalent.service.impl.PromptServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PromptService 单元测试
 *
 * @author 小阳
 * @date 2025-12-28
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class PromptServiceTest {

    @Mock
    private PromptConfigMapper promptConfigMapper;

    @InjectMocks
    private PromptServiceImpl promptService;

    private PromptConfigEntity mockEntity;

    @BeforeEach
    void setUp() {
        mockEntity = new PromptConfigEntity();
        mockEntity.setPromptKey("TEST_KEY");
        mockEntity.setLanguage("zh-CN");
        mockEntity.setContent("Test Content");
        mockEntity.setVersion(1);
        mockEntity.setActive(true);
    }

    @Test
    void testGetPrompt_CacheHit() {
        // 1. Arrange: 模拟数据库返回，用于第一次加载
        when(promptConfigMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockEntity);
        
        // 2. Act: 第一次调用，会查库并缓存
        String content1 = promptService.getPrompt("TEST_KEY");
        
        // 3. Act: 第二次调用，应该走缓存 (不需要再次 Mock)
        String content2 = promptService.getPrompt("TEST_KEY");
        
        // 4. Assert
        assertEquals("Test Content", content1);
        assertEquals("Test Content", content2);
        
        // 验证数据库只查了一次
        verify(promptConfigMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetPrompt_DatabaseFallback() {
        // Arrange
        when(promptConfigMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockEntity);

        // Act
        String content = promptService.getPrompt("TEST_KEY");

        // Assert
        assertEquals("Test Content", content);
    }

    @Test
    void testGetPrompt_NotFound() {
        // Arrange
        when(promptConfigMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            promptService.getPrompt("UNKNOWN_KEY");
        });
    }

    @Test
    void testRefreshCache() {
        // Arrange
        when(promptConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(mockEntity));

        // Act
        promptService.refreshCache();

        // Assert: 验证调用了 selectList
        verify(promptConfigMapper).selectList(any(LambdaQueryWrapper.class));
        
        // 验证缓存是否生效 (可以通过 getPrompt 不查库来验证)
        String content = promptService.getPrompt("TEST_KEY");
        assertEquals("Test Content", content);
        
        // selectOne 应该一次都没调用，因为直接从 refreshCache 填充了
        verify(promptConfigMapper, never()).selectOne(any(LambdaQueryWrapper.class));
    }
}
