package com.paul.appgen.ai;

import com.esotericsoftware.minlog.Log;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.paul.appgen.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
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

    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private StreamingChatModel streamingChatModel;

    @Resource
    private ChatHistoryService chatHistoryService;

    private final Cache<Long,AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key,value,cause) -> {
                log.debug("AI 服务实例被移除！appId:{},原因:{}",key,cause);
            })
            .build();

    private AiCodeGeneratorService createAiCodeGeneratorService(long appId){
        log.info("创建AI服务实例，appId:{}",appId);
        // 创建基于Redis的聊天记忆实例，设置最大消息数为20
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId) // 使用传入的appId作为聊天记忆的ID
                .chatMemoryStore(redisChatMemoryStore) // 设置Redis作为聊天记忆存储
                .maxMessages(20) // 设置最大保留的消息数量
                .build();
        // 构建并返回AI代码生成服务实例，配置聊天模型、流式聊天模型和聊天记忆
        chatHistoryService.loadChatHistoryToMemory(appId,chatMemory,20);
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel) // 设置常规聊天模型
                .streamingChatModel(streamingChatModel) // 设置流式聊天模型
                .chatMemory(chatMemory) // 设置聊天记忆
                .build();
    }

    /**
     * 根据appId获取Ai代码生成器服务
     * @param appId
     * @return
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return serviceCache.get(appId, this::createAiCodeGeneratorService);
    }



    /**
     * 创建Ai代码生成器服务
     * 流式调用
     *
     * @return
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0);
    }


}
