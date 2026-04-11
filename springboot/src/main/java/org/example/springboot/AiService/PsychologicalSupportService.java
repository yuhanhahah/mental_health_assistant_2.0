package org.example.springboot.AiService;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.DTO.response.ConsultationMessageResponseDTO;
import org.example.springboot.DTO.command.ConsultationSessionCreateDTO;
import org.example.springboot.entity.ConsultationSession;
import org.example.springboot.service.ConsultationMessageService;
import org.example.springboot.service.ConsultationSessionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.Arrays;
import java.util.List;

/**
 * 流式心理疏导智能对话机器人服务
 * 基于Spring AI实现的流式心理疏导系统
 * 提供连续对话、实时风险评估、会话管理等功能
 */
@Slf4j
@Service
public class PsychologicalSupportService {

    @Autowired
    @Qualifier("open-ai")
    private ChatClient chatClient;

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private ConsultationSessionService consultationSessionService;

    @Autowired
    private ConsultationMessageService consultationMessageService;

    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
            "自杀", "轻生", "不想活", "自残", "割腕", "吞药", "绝望", "结束生命"
    );

    private static final String CRISIS_REPLY = "【系统安全提示】检测到您当前可能处于心理危机状态。请立即寻求专业医疗帮助，或拨打全国24小时心理危机干预热线（如希望24小时热线：400-161-9995）。生命只有一次，请给自己一个机会，我们都在关心你。";

    /**
     * 开始新的心理疏导会话
     *
     * @param userId 用户ID
     * @param createDTO 创建DTO
     * @return 会话信息
     */
    public StructOutPut.StreamChatSession startChatSession(Long userId, ConsultationSessionCreateDTO createDTO) {
        log.info("开始新的心理疏导会话，用户: {}", userId);

        try {
            // 创建数据库会话记录
            ConsultationSession dbSession = consultationSessionService.createSession(userId, createDTO);

            // 【精确修改】：增加判空逻辑。防止前端未传递 initialMessage 时将 null 存入数据库引发 SQLException
            if (createDTO.getInitialMessage() != null && !createDTO.getInitialMessage().trim().isEmpty()) {
                consultationMessageService.saveUserMessage(dbSession.getId(), createDTO.getInitialMessage(), null);
            }

            // 创建会话ID（使用数据库会话ID）
            String sessionId = "session_" + dbSession.getId();
            String conversationId = generateConversationId(sessionId);

            // 创建会话信息
            StructOutPut.StreamChatSession session = new StructOutPut.StreamChatSession(
                    sessionId,
                    userId,
                    createDTO.getInitialMessage(),
                    System.currentTimeMillis(),
                    System.currentTimeMillis() + 86400000L, // 24小时过期
                    "ACTIVE",
                    new ArrayList<>(),
                    1 // 初始消息计数
            );

            // 添加初始消息到ChatMemory
            if (createDTO.getInitialMessage() != null && !createDTO.getInitialMessage().trim().isEmpty()) {
                List<Message> messages = new ArrayList<>();
                messages.add(new UserMessage(createDTO.getInitialMessage()));
                chatMemory.add(conversationId, messages);
                log.info("初始消息已添加到ChatMemory，conversationId: {}", conversationId);
            }

            log.info("心理疏导会话创建成功，会话ID: {}，数据库会话ID: {}", sessionId, dbSession.getId());
            return session;

        } catch (Exception e) {
            log.error("创建心理疏导会话失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建会话失败: " + e.getMessage());
        }
    }


    /**
     * 流式心理疏导对话
     */
    public Flux<String> streamPsychologicalChat(String sessionId, String userMessage) {
        log.info("开始流式心理疏导对话，会话ID: {}, 用户消息: {}", sessionId, userMessage);

        return Flux.create(sink -> {
            try {
                // 【新增：精确的敏感词前置拦截逻辑】
                if (userMessage != null) {
                    for (String word : SENSITIVE_WORDS) {
                        if (userMessage.contains(word)) {
                            log.warn("触发敏感词拦截，会话ID: {}, 敏感词: {}", sessionId, word);

                            Long dbSessionId = extractSessionId(sessionId);
                            if (dbSessionId != null) {
                                // 确保持久化用户的危险发言
                                consultationMessageService.saveUserMessage(dbSessionId, userMessage, null);
                                // 将系统警告作为 AI 回复存入数据库，senderType 标注为系统
                                consultationMessageService.saveAiMessage(dbSessionId, CRISIS_REPLY, "system_alert");
                            }

                            // 向前端推送干预话术，并正常关闭 SSE 流
                            sink.next(CRISIS_REPLY);
                            sink.complete();
                            return; // 核心：直接 return 阻断下方所有逻辑，不再调用大模型
                        }
                    }
                }

                // 验证数据库会话
                Long dbSessionId = extractSessionId(sessionId);
                if (dbSessionId == null) {
                    sink.error(new RuntimeException("无效的会话ID格式"));
                    return;
                }

                // 验证会话是否存在且活跃
                ConsultationSession dbSession = consultationSessionService.getSessionById(dbSessionId);
                if (dbSession == null) {
                    sink.error(new RuntimeException("会话不存在"));
                    return;
                }



                // 生成conversationId用于ChatMemory
                String conversationId = generateConversationId(sessionId);

                // 保存用户消息到数据库（检查是否为初始消息，避免重复保存）
                try {
                    Integer messageCount = consultationMessageService.getMessageCountBySessionId(dbSessionId);
                    boolean isInitialMessage = false;
                    
                    if (messageCount == 1) {
                        ConsultationMessageResponseDTO lastMessage = consultationMessageService.getLastMessageBySessionId(dbSessionId);
                        if (lastMessage != null && lastMessage.getSenderType() == 1 && 
                            userMessage.equals(lastMessage.getContent())) {
                            isInitialMessage = true;
                            log.info("检测到重复的初始消息，跳过保存");
                        }
                    }
                    
                    if (!isInitialMessage) {
                        consultationMessageService.saveUserMessage(dbSessionId, userMessage, null);
                        log.info("用户消息已保存到数据库");
                    }
                } catch (Exception e) {
                    log.warn("保存用户消息到数据库失败: {}", e.getMessage());
                }

                // 添加用户消息到ChatMemory
                List<Message> userMessages = new ArrayList<>();
                userMessages.add(new UserMessage(userMessage));
                chatMemory.add(conversationId, userMessages);

                // 异步执行情绪分析
                CompletableFuture.runAsync(() -> {
                    try {
                        log.info("开始异步情绪分析，用户消息: {}", userMessage);
                        StructOutPut.EmotionAnalysisResult emotionAnalysis = analyzeUserEmotion(userMessage);
                        
                        // 更新情绪分析到数据库会话表
                        try {
                            JSONObject emotionJson = new JSONObject();
                            emotionJson.set("primaryEmotion", emotionAnalysis.primaryEmotion());
                            emotionJson.set("emotionScore", emotionAnalysis.emotionScore());
                            emotionJson.set("isNegative", emotionAnalysis.isNegative());
                            emotionJson.set("riskLevel", emotionAnalysis.riskLevel());
                            emotionJson.set("keywords", emotionAnalysis.keywords());
                            emotionJson.set("suggestion", emotionAnalysis.suggestion());
                            emotionJson.set("icon", emotionAnalysis.icon());
                            emotionJson.set("label", emotionAnalysis.label());
                            emotionJson.set("riskDescription", emotionAnalysis.riskDescription());
                            emotionJson.set("improvementSuggestions", emotionAnalysis.improvementSuggestions());
                            emotionJson.set("timestamp", emotionAnalysis.timestamp());
                            
                            consultationSessionService.updateLastEmotionAnalysis(dbSessionId, JSONUtil.toJsonStr(emotionJson));
                        } catch (Exception e) {
                            log.warn("更新数据库情绪分析失败: {}", e.getMessage());
                        }

                        log.info("异步情绪分析完成，emotion={}, riskLevel={}",
                                emotionAnalysis.primaryEmotion(), emotionAnalysis.riskLevel());

                    } catch (Exception e) {
                        log.error("异步情绪分析失败: {}", e.getMessage(), e);
                    }
                });

                StringBuilder fullResponse = new StringBuilder();
                Prompt prompt = new Prompt(List.of(
                        new SystemMessage(PromptManage.PSYCHOLOGICAL_SUPPORT_SYSTEM_PROMPT)
                ));

                // 使用ChatClient进行对话，ChatMemory会自动管理上下文
                chatClient.prompt(prompt)
                        .user(userMessage)
                        .advisors(advisorSpec -> advisorSpec
                                .param(ChatMemory.CONVERSATION_ID, conversationId))
                        .stream()
                        .content()
                        .doOnNext(fragment -> {
                            fullResponse.append(fragment);
                            sink.next(fragment);
                        })
                        .doOnComplete(() -> {
                            String completeResponse = fullResponse.toString();

                            // 保存AI回复到数据库
                            try {
                                consultationMessageService.saveAiMessage(dbSessionId, completeResponse, "openai");
                            } catch (Exception e) {
                                log.warn("保存AI回复到数据库失败: {}", e.getMessage());
                            }

                            // 添加AI回复到ChatMemory
                            List<Message> assistantMessages = new ArrayList<>();
                            assistantMessages.add(new AssistantMessage(completeResponse));
                            chatMemory.add(conversationId, assistantMessages);

                            sink.complete();
                            log.info("流式心理疏导对话完成，会话ID: {}", sessionId);
                        })
                        .doOnError(error -> {
                            log.error("流式心理疏导对话异常: {}", error.getMessage(), error);
                            sink.error(error);
                        })
                        .subscribe();

            } catch (Exception e) {
                log.error("流式心理疏导对话初始化失败: {}", e.getMessage(), e);
                sink.error(e);
            }
        });
    }



    /**
     * 结束会话
     */
    public boolean endChatSession(String sessionId, Integer moodAfter) {
        try {
            log.info("结束心理疏导会话，会话ID: {}", sessionId);

            // 提取数据库会话ID
            Long dbSessionId = extractSessionId(sessionId);
            if (dbSessionId == null) {
                log.error("无效的会话ID格式: {}", sessionId);
                return false;
            }

            // 获取会话信息以验证用户权限
            ConsultationSession dbSession = consultationSessionService.getSessionById(dbSessionId);
            if (dbSession == null) {
                log.error("会话不存在: {}", dbSessionId);
                return false;
            }



            // 清理ChatMemory中的对话历史
            String conversationId = generateConversationId(sessionId);
            try {
                chatMemory.clear(conversationId);
                log.info("ChatMemory已清理，conversationId: {}", conversationId);
            } catch (Exception e) {
                log.warn("清理ChatMemory失败: {}", e.getMessage());
            }

            log.info("心理疏导会话结束成功，会话ID: {}", sessionId);
            return true;

        } catch (Exception e) {
            log.error("结束心理疏导会话失败: {}", e.getMessage(), e);
            return false;
        }
    }



    /**
     * 从sessionId中提取数据库会话ID
     */
    public Long extractSessionId(String sessionId) {
        try {
            if (sessionId != null && sessionId.startsWith("session_")) {
                String idStr = sessionId.substring("session_".length());
                return Long.parseLong(idStr);
            }
            return null;
        } catch (Exception e) {
            log.error("提取会话ID失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 生成conversationId（ChatMemory使用）
     */
    private String generateConversationId(String sessionId) {
        return "conversation_" + sessionId;
    }







    /**
     * 获取默认的情绪分析结果
     */
    public StructOutPut.EmotionAnalysisResult getDefaultEmotionAnalysis() {
        return new StructOutPut.EmotionAnalysisResult(
                "中性",
                50,  // 情绪强度评分改为百分制
                false,
                0,
                new ArrayList<>(),
                "情绪状态平稳，慢慢来就好",
                "😐",
                "平静",
                "当前情绪状态稳定，无需特别关注",
                List.of("保持规律作息", "适当运动", "与朋友交流"),
                System.currentTimeMillis()
        );
    }




    /**
     * 快速情绪分析
     *
     * @param content 用户内容
     * @return 情绪分析结果
     */
    public StructOutPut.EmotionAnalysisResult analyzeUserEmotion(String content) {
        log.info("开始快速情绪分析");

        try {
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(PromptManage.EMOTION_ANALYSIS_SYSTEM_PROMPT)
            ));

            StructOutPut.EmotionAnalysisResult result = chatClient
                    .prompt(prompt)
                    .user("请快速分析以下内容的情绪状态：\n" + content)
                    .call()
                    .entity(StructOutPut.EmotionAnalysisResult.class);

            log.info("情绪分析完成: emotion={}, riskLevel={}", result.primaryEmotion(), result.riskLevel());
            return result;

        } catch (Exception e) {
            log.error("情绪分析失败: {}", e.getMessage(), e);
            return getDefaultEmotionAnalysis();
        }
    }







}