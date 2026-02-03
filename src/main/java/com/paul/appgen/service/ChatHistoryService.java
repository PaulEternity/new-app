package com.paul.appgen.service;

import com.mybatisflex.core.service.IService;
import com.paul.appgen.model.entity.ChatHistory;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com"></a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    boolean addChatMessage(Long appId,String message,String messageType,Long userId);

}
