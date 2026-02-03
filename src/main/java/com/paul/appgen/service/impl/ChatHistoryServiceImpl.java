package com.paul.appgen.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.paul.appgen.constant.UserConstant;
import com.paul.appgen.model.entity.User;
import com.paul.appgen.exception.ErrorCode;
import com.paul.appgen.exception.ThrowUtils;
import com.paul.appgen.model.dto.chathistory.ChatHistoryQueryRequest;
import com.paul.appgen.model.entity.App;
import com.paul.appgen.model.entity.ChatHistory;
import com.paul.appgen.mapper.ChatHistoryMapper;
import com.paul.appgen.model.enums.ChatHistoryMessageTypeEnum;
import com.paul.appgen.service.AppService;
import com.paul.appgen.service.ChatHistoryService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层实现。
 *
 * @author <a href="https://github.com"></a>
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

    @Resource
    @Lazy
    private AppService appService;

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

    @Override
    /**
     * 根据应用ID删除记录
     * @param appId 应用ID，必须大于0
     * @return 删除是否成功
     * @throws ErrorCode.PARAMS_ERROR 当appId为null或小于等于0时抛出
     */
    public boolean deleteByAppId(Long appId) {
        // 参数校验：appId不能为null且必须大于0，否则抛出参数错误异常
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR,"应用ID不能为空");
        // 创建查询条件，设置app_id等于传入的appId
        QueryWrapper queryWrapper = QueryWrapper.create().eq("app_id",appId);
        // 执行删除操作并返回结果
        return this.remove(queryWrapper);
    }

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("messageType", messageType)
                .eq("appId", appId)
                .eq("userId", userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                      LocalDateTime lastCreateTime,
                                                      User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 验证权限：只有应用创建者和管理员可以查看
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");
        // 构建查询条件
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
        // 查询数据
        return this.page(Page.of(1, pageSize), queryWrapper);
    }



}
