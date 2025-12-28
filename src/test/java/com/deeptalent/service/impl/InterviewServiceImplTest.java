package com.deeptalent.service.impl;

import com.deeptalent.domain.DeepTalentState;
import com.deeptalent.domain.EvaluationResult;
import com.deeptalent.domain.Message;
import com.deeptalent.domain.Phase;
import com.deeptalent.service.PersistenceService;
import com.deeptalent.service.PromptService;
import com.deeptalent.service.ai.DeepTalentAgent;
import dev.langchain4j.data.message.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * InterviewService 单元测试
 *
 * @author 小阳
 * @date 2025-12-28
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
public class InterviewServiceImplTest {

    @Mock
    private DeepTalentAgent deepTalentAgent;

    @Mock
    private PersistenceService persistenceService;

    @Mock
    private PromptService promptService;

    @InjectMocks
    private InterviewServiceImpl interviewService;

    private DeepTalentState mockState;
    private String threadId = "test-thread-001";

    @BeforeEach
    void setUp() {
        mockState = new DeepTalentState();
        mockState.setMessages(new ArrayList<>());
        mockState.setCurrentPhase(Phase.CHILDHOOD);
        
        // Leniently mock prompt service because not all tests use it in the same way, 
        // but we want to avoid NullPointerException if it is called.
        lenient().when(promptService.getPrompt(anyString())).thenReturn("Prompt Template");
    }

    @Test
    void testStartSession_NewSession() {
        // Arrange
        when(persistenceService.loadState(threadId)).thenReturn(new DeepTalentState());
        when(deepTalentAgent.chat(anyList())).thenReturn("你好！我是深度天赋挖掘机");

        // Act
        String response = interviewService.startSession(threadId);

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("你好！我是深度天赋挖掘机"));
        verify(persistenceService, times(1)).saveState(eq(threadId), any(DeepTalentState.class));
    }

    @Test
    void testProcessMessage_NormalFlow() {
        // Arrange
        when(persistenceService.loadState(threadId)).thenReturn(mockState);
        
        // Mock Evaluator response
        EvaluationResult evalResult = new EvaluationResult();
        evalResult.setScore(8);
        evalResult.setNeedFollowup(false);
        when(deepTalentAgent.evaluate(anyList())).thenReturn(evalResult);

        // Mock Chat response
        when(deepTalentAgent.chat(anyList())).thenReturn("这是下一个问题");

        // Act
        String response = interviewService.chat(threadId, "我小时候喜欢画画");

        // Assert
        assertEquals("这是下一个问题", response);
        verify(deepTalentAgent).evaluate(anyList());
        verify(deepTalentAgent).chat(anyList());
        verify(persistenceService).saveState(eq(threadId), any(DeepTalentState.class));
        
        // Verify state update
        assertEquals(1, mockState.getDialogueCount());
        assertEquals(2, mockState.getMessages().size()); // User + Assistant
    }

    @Test
    void testProcessMessage_NeedFollowup() {
        // Arrange
        when(persistenceService.loadState(threadId)).thenReturn(mockState);

        // Mock Evaluator response (Need Followup)
        EvaluationResult evalResult = new EvaluationResult();
        evalResult.setNeedFollowup(true);
        evalResult.setFollowupQuestion("具体画了什么？");
        when(deepTalentAgent.evaluate(anyList())).thenReturn(evalResult);
        
        // Mock Chat response (Interviewer generates the followup question based on eval)
        when(deepTalentAgent.chat(anyList())).thenReturn("具体画了什么？");

        // Act
        String response = interviewService.chat(threadId, "我喜欢画画");

        // Assert
        assertEquals("具体画了什么？", response);
        verify(deepTalentAgent).chat(anyList()); // Should call chat to generate the followup
        assertTrue(mockState.isNeedFollowup());
    }
}
