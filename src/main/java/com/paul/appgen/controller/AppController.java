package com.paul.appgen.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;

import com.paul.appgen.annotation.AuthCheck;
import com.paul.appgen.common.BaseResponse;
import com.paul.appgen.common.DeleteRequest;
import com.paul.appgen.common.ResultUtils;
import com.paul.appgen.constant.AppConstant;
import com.paul.appgen.constant.UserConstant;
import com.paul.appgen.exception.BusinessException;
import com.paul.appgen.exception.ErrorCode;
import com.paul.appgen.exception.ThrowUtils;
import com.paul.appgen.model.dto.app.*;
import com.paul.appgen.model.entity.App;
import com.paul.appgen.model.entity.User;
import com.paul.appgen.model.vo.AppVO;
import com.paul.appgen.service.AppService;
import com.paul.appgen.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 应用 控制层。
 *
 * @author <a href="https://github.com"></a>
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId, @RequestParam String message, HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID错误");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "提示词不能为空");
        User loginUser = userService.getLoginUser(request);
        Flux<String> contentFlux = appService.chatToGenCode(appId, message, loginUser);
        return contentFlux.map(chunk -> {
                    Map<String, String> wrapper = Map.of("d", chunk);
                    String jsonData = JSONUtil.toJsonStr(wrapper);
                    return ServerSentEvent.<String>builder()
                            .data(jsonData)
                            .build();
                })
                .concatWith(Mono.just(
                        ServerSentEvent.<String>builder()
                                .event("done")
                                .data("")
                                .build()
                ));
    }

    /**
     * 添加应用程序接口
     *
     * @param appAddRequest 添加应用程序的请求参数
     * @param request       HTTP请求对象
     * @return 返回新添加的应用程序ID
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        // 参数校验：如果请求参数为空，则抛出参数错误异常
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
        // 调用服务层方法添加应用程序，并获取新添加的应用程序ID
        Long appId = appService.createApp(appAddRequest, loginUser);
        // 返回成功响应，包含新添加的应用程序ID
        return ResultUtils.success(appId);
    }

    /**
     * 应用部署
     *
     * @param appDeployRequest 部署请求
     * @param request          请求
     * @return 部署 URL
     */
    @PostMapping("/deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        // 检查部署请求是否为空
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取应用 ID
        Long appId = appDeployRequest.getAppId();
        // 检查应用 ID 是否为空
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务部署应用
        String deployUrl = appService.deployApp(appId, loginUser);
        // 返回部署 URL
        return ResultUtils.success(deployUrl);
    }

    /**
     * 更新当前用户的应用信息
     *
     * @param appUpdateMyRequest 包含应用更新信息的请求对象
     * @param request            HTTP请求对象，用于获取当前登录用户信息
     * @return 返回操作结果，成功返回true，失败抛出异常
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateMyRequest appUpdateMyRequest,
                                           HttpServletRequest request) {
        // 检查请求参数是否为空
        ThrowUtils.throwIf(appUpdateMyRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
        Long id = appUpdateMyRequest.getId();
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人可更新
        if (!oldApp.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        App app = new App();
        app.setId(id);
        app.setAppName(appUpdateMyRequest.getAppName());
        // 设置编辑时间
        app.setEditTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 删除当前用户的应用
     *
     * @param deleteRequest 包含要删除的应用ID的请求对象
     * @param request       HTTP请求对象，用于获取当前登录用户信息
     * @return 返回操作结果，成功返回true，失败抛出异常
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest,
                                           HttpServletRequest request) {
        // 检查请求参数是否为空
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        // 从请求中获取应用ID
        Long id = deleteRequest.getId();
        // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        App oldApp = appService.getById(id);
        // 检查ID是否有效
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        // 检查应用是否存在
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 检查用户是否有权限删除该应用（必须是应用所有者或管理员）
        if (!oldApp.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 调用服务层方法删除应用并返回结果
        return ResultUtils.success(appService.deleteAppByUser(id, loginUser));
    }

    /**
     * 获取当前用户的应用信息
     *
     * @param id      应用ID
     * @param request HTTP请求对象，用于获取当前登录用户信息
     * @return BaseResponse<AppVO> 返回应用视图对象
     */
    @GetMapping("/get/vo")
    public BaseResponse<AppVO> getAppVOById(@RequestParam Long id, HttpServletRequest request) {
        // 参数校验：检查ID是否为空或小于等于0
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
        // 根据ID获取应用信息
        App app = appService.getById(id);
        // 校验应用是否存在
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 权限校验：检查当前用户是否为应用的所有者
        ThrowUtils.throwIf(!loginUser.getId().equals(app.getUserId()), ErrorCode.NO_AUTH_ERROR);
        // 返回应用视图对象
        return ResultUtils.success(appService.getAppVO(app));
    }

    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppVO>> listMyAppVOByPage(@RequestBody AppQueryMyRequest appQueryMyRequest,
                                                       HttpServletRequest request) {
        ThrowUtils.throwIf(appQueryMyRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        // 限制每页最多 20 个
        long pageSize = appQueryMyRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
        long pageNum = appQueryMyRequest.getPageNum();
        // 只查询当前用户的应用
        appQueryMyRequest.setUserId(loginUser.getId());
        QueryWrapper queryWrapper = appService.getMyQueryWrapper(appQueryMyRequest, loginUser.getId());
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 分页查询优质应用列表接口
     *
     * @param appQueryRequest 分页查询请求参数，包含应用名称、页码和每页大小
     * @return BaseResponse<Page < AppVO>> 返回分页查询结果，包含应用视图对象列表
     */
    @PostMapping("/good/list/page/vo")
    public BaseResponse<Page<AppVO>> listGoodAppVOByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 限制每页最多 20 个
        long pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
        long pageNum = appQueryRequest.getPageNum();
        // 只查询精选的应用
        appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        // 分页查询
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 删除应用接口
     *
     * @param deleteRequest 删除请求参数，包含要删除的应用ID
     * @return 删除操作是否成功的结果
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)  // 权限检查，只有管理员角色可以访问此接口
    public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest) {
        // 参数校验：如果请求参数为空，则抛出参数错误异常
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取要删除的ID
        Long id = deleteRequest.getId();
        // 参数校验：如果ID为空或小于等于0，则抛出参数错误异常
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        // 调用服务层方法删除应用，并返回操作结果
        return ResultUtils.success(appService.removeById(id));
    }

    /**
     * 更新应用程序接口
     * 该接口用于管理员更新应用程序信息
     * 需要管理员权限才能访问
     *
     * @param appAdminUpdateRequest 包含要更新的应用程序信息的请求体
     * @return 返回操作结果，成功返回true，失败返回false
     */
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)  // 权限检查，仅允许管理员访问
    public BaseResponse<Boolean> updateAppByAdmin(@RequestBody AppUpdateRequest appAdminUpdateRequest) {
        if (appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        long id = appAdminUpdateRequest.getId();
        // 判断是否存在
        App oldApp = appService.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        App app = new App();
        BeanUtil.copyProperties(appAdminUpdateRequest, app);
        // 设置编辑时间
        app.setEditTime(LocalDateTime.now());
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 分页查询应用列表
     * 该接口需要管理员权限才能访问
     *
     * @param appQueryRequest 分页查询请求参数，包含分页信息和查询条件
     * @return BaseResponse<Page < AppVO>> 返回分页查询结果，包含应用视图对象列表
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)  // 权限检查，仅允许管理员访问
    public BaseResponse<Page<AppVO>> listAppVOByPageByAdmin(@RequestBody AppQueryRequest appQueryRequest) {
        // 参数校验，如果请求参数为空则抛出参数错误异常
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 执行分页查询，获取应用数据列表
        Page<App> appPage = appService.page(
                new Page<>(appQueryRequest.getPageNum(), appQueryRequest.getPageSize()),  // 设置分页参数
                appService.getQueryWrapper(appQueryRequest));  // 获取查询条件构造器
        // 将查询结果转换为视图对象并返回成功响应
        return ResultUtils.success(appService.getAppVOPage(appPage));
    }

    /**
     * 根据应用ID获取应用信息
     * 需要管理员权限才能访问
     *
     * @param id 应用ID
     * @return 返回应用视图对象(BaseResponse < AppVO >)
     */
    @GetMapping("/admin/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)  // 权限检查，只有管理员角色可以访问
    public BaseResponse<AppVO> getAppVOByIdByAdmin(@RequestParam Long id) {
        // 参数校验：检查ID是否为空或小于等于0
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        // 根据ID查询应用
        App app = appService.getById(id);
        // 检查应用是否存在
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 返回应用视图对象
        return ResultUtils.success(appService.getAppVO(app));
    }

}
