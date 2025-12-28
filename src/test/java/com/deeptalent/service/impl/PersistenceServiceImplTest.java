package com.deeptalent.service.impl;

import com.deeptalent.domain.DeepTalentState;
import com.deeptalent.domain.Extraction;
import com.deeptalent.domain.Message;
import com.deeptalent.domain.Phase;
import com.deeptalent.mapper.ConversationMapper;
import com.deeptalent.mapper.ExtractionMapper;
import com.deeptalent.mapper.MessageMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootTest
class PersistenceServiceImplTest {

    @Autowired
    private PersistenceServiceImpl persistenceService;

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private ExtractionMapper extractionMapper;

    @Test
    @Transactional // 测试完成后自动回滚
    void testSaveAndLoadState() {
        // 1. 准备测试数据
        String threadId = UUID.randomUUID().toString();
        DeepTalentState originalState = new DeepTalentState();
        
        // 设置基本属性
        originalState.setCurrentPhase(Phase.COMPETENCE);
        originalState.setDialogueCount(5);
        originalState.setNeedFollowup(true);
        originalState.setFinalReport("Test Report Content");

        // 设置消息
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", "System Prompt"));
        messages.add(new Message("user", "User Input"));
        messages.add(new Message("assistant", "AI Response"));
        originalState.setMessages(messages);

        // 设置特征提取
        List<Extraction> extractions = new ArrayList<>();
        extractions.add(new Extraction("Leadership", "Led a team", "competence", 0.9));
        originalState.getUserProfile().put("competence", extractions);

        // 2. 执行保存
        persistenceService.saveState(threadId, originalState);

        // 3. 执行加载
        DeepTalentState loadedState = persistenceService.loadState(threadId);

        // 4. 验证数据一致性
        Assertions.assertNotNull(loadedState);
        Assertions.assertEquals(originalState.getCurrentPhase(), loadedState.getCurrentPhase());
        Assertions.assertEquals(originalState.getDialogueCount(), loadedState.getDialogueCount());
        Assertions.assertEquals(originalState.isNeedFollowup(), loadedState.isNeedFollowup());
        Assertions.assertEquals(originalState.getFinalReport(), loadedState.getFinalReport());
        
        // 验证消息数量和内容
        Assertions.assertEquals(3, loadedState.getMessages().size());
        Assertions.assertEquals("User Input", loadedState.getMessages().get(1).getContent());

        // 验证特征提取
        Assertions.assertTrue(loadedState.getUserProfile().containsKey("competence"));
        Assertions.assertEquals(1, loadedState.getUserProfile().get("competence").size());
        Assertions.assertEquals("Leadership", loadedState.getUserProfile().get("competence").get(0).getTag());
    }
}
