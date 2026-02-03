package com.paul.appgen.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.paul.appgen.exception.BusinessException;
import com.paul.appgen.exception.ErrorCode;
import com.paul.appgen.exception.ThrowUtils;
import com.paul.appgen.model.entity.ChatHistory;
import com.paul.appgen.mapper.ChatHistoryMapper;
import com.paul.appgen.model.enums.ChatHistoryMessageTypeEnum;
import com.paul.appgen.service.ChatHistoryService;
import org.springframework.stereotype.Service;

/**
 * 对话历史 服务层实现。
 *
 * @author <a href="https://github.com"></a>
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

/**
 * 添加聊天消息
 * @param appId 应用ID
 * @param message 消息内容
 * @param messageType 消息类型
 * @param userId 用户ID
 * @return 是否添加成功
 */
    @Override
    public boolean addChatMessage(Long appId, String message,String messageType, Long userId) {
    // 校验应用ID是否为空或小于等于0
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR,"应用ID不能为空");
    // 校验消息内容是否为空
        ThrowUtils.throwIf(message == null || message.trim().length() == 0, ErrorCode.PARAMS_ERROR,"消息不能为空");
    // 校验用户ID是否为空或小于等于0
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR,"用户ID不能为空");
    // 根据消息类型值获取对应的枚举
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
    // 校验消息类型是否有效
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR,"不支持的消息类型");
    // 构建聊天历史记录对象
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageType)
                .userId(userId)
                .build();

    // 保存聊天历史记录并返回保存结果
        return this.save(chatHistory);
    }
}
