package com.paul.appgen.core.handler;

import com.paul.appgen.model.entity.User;
import com.paul.appgen.model.enums.ChatHistoryMessageTypeEnum;
import com.paul.appgen.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 简单文本流处理器
 * 用于处理AI流式响应，并将响应内容记录到对话历史中
 */
@Slf4j
public class SimpleTextStreamHandler {
    /**
     * 处理AI流式响应的方法
     * @param originFlux 原始流式响应数据
     * @param chatHistoryService 对话历史服务
     * @param appId 应用ID
     * @param loginUser 登录用户信息
     * @return 处理后的流式数据
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User loginUser) {
        // 用于构建完整的AI响应内容
        StringBuilder aiResponseBuilder = new StringBuilder();
        return originFlux
                // 处理每个流式数据块
                .map(chunk -> {
                    // 收集AI响应内容
                    aiResponseBuilder.append(chunk);
                    return chunk;
                })
                // 流式响应完成时的回调
                .doOnComplete(() -> {
                    // 流式响应完成后，添加AI消息到对话历史
                    String aiResponse = aiResponseBuilder.toString();
                    chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                })
                // 处理流式响应过程中的错误
                .doOnError(error -> {
                    // 如果AI回复失败，也要记录错误消息
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                });
    }
}
