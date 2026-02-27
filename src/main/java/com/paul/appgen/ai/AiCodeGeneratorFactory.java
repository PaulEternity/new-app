package com.paul.appgen.ai;

import com.esotericsoftware.minlog.Log;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.paul.appgen.ai.tools.FileWriteTool;
import com.paul.appgen.exception.BusinessException;
import com.paul.appgen.exception.ErrorCode;
import com.paul.appgen.model.enums.CodeGenTypeEnum;
import com.paul.appgen.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Ai服务创建工厂
 */
@Configuration
@Slf4j
public class AiCodeGeneratorFactory {


    private ChatModel chatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private StreamingChatModel openAiStreamingChatModel;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private StreamingChatModel reasoningStreamingChatModel;

    private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除！缓存键:{},原因:{}", key, cause);
            })
            .build();

    private AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
        log.info("创建AI服务实例，appId:{}", appId);
        // 创建基于Redis的聊天记忆实例，设置最大消息数为20
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId) // 使用传入的appId作为聊天记忆的ID
                .chatMemoryStore(redisChatMemoryStore) // 设置Redis作为聊天记忆存储
                .maxMessages(20) // 设置最大保留的消息数量
                .build();
        // 构建并返回AI代码生成服务实例，配置聊天模型、流式聊天模型和聊天记忆
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
        return switch (codeGenType) {
            case HTML, MULTI_FILE -> AiServices.builder(AiCodeGeneratorService.class)
                    .chatModel(chatModel) // 设置常规聊天模型
                    .streamingChatModel(openAiStreamingChatModel) // 设置流式聊天模型
                    .chatMemory(chatMemory) // 设置聊天记忆
                    .build();

            case VUE_PROJECT -> AiServices.builder(AiCodeGeneratorService.class)
                    .chatModel(chatModel) // 设置常规聊天模型
                    .streamingChatModel(reasoningStreamingChatModel) // 设置流式聊天模型
                    .chatMemoryProvider(memoryId -> chatMemory)
                    .tools(new FileWriteTool())
                    //处理工具调用幻觉问题
                    .hallucinatedToolNameStrategy(toolExecutionRequest ->
                            ToolExecutionResultMessage
                                    .from(toolExecutionRequest, "Error:there is no tool called " + toolExecutionRequest.name()))
                    .build();
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型" + codeGenType.getValue());
        };

    }

    /**
     * 根据appId获取Ai代码生成器服务
     *
     * @param appId
     * @return
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId,CodeGenTypeEnum codeGenType) {
        String cacheKey = buildCacheKey(appId, codeGenType);
        return serviceCache.get(cacheKey, key -> createAiCodeGeneratorService(appId, codeGenType));
    }

    /**
     * 根据appId获取Ai代码生成器服务
     * 为了兼容老逻辑
     * @param appId
     * @return
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
    }


    private String buildCacheKey(long appId, CodeGenTypeEnum codeGenType) {
        return appId + "_" + codeGenType.getValue();
    }


}
