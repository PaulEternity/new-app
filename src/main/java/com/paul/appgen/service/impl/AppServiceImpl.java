package com.paul.appgen.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.paul.appgen.constant.AppConstant;
import com.paul.appgen.exception.BusinessException;
import com.paul.appgen.exception.ErrorCode;
import com.paul.appgen.mapper.AppMapper;
import com.paul.appgen.model.dto.app.AppAddRequest;
import com.paul.appgen.model.dto.app.AppQueryMyRequest;
import com.paul.appgen.model.dto.app.AppQueryRequest;
import com.paul.appgen.model.dto.app.AppUpdateMyRequest;
import com.paul.appgen.model.dto.app.AppUpdateRequest;
import com.paul.appgen.model.entity.App;
import com.paul.appgen.model.entity.User;
import com.paul.appgen.model.vo.AppVO;
import com.paul.appgen.service.AppService;
import com.paul.appgen.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com"></a>
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Override
    public long createApp(AppAddRequest appAddRequest, User loginUser) {
        if (appAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }
        String initPrompt = appAddRequest.getInitPrompt();
        if (StrUtil.isBlank(initPrompt)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "initPrompt 不能为空");
        }
        App app = new App();
        String appName = appAddRequest.getAppName();
        app.setAppName(StrUtil.isBlank(appName) ? "未命名应用" : appName);
        app.setCover(appAddRequest.getCover());
        app.setInitPrompt(initPrompt);
        app.setCodeGenType(appAddRequest.getCodeGenType());
        app.setUserId(loginUser.getId());
        app.setPriority(AppConstant.DEFAULT_APP_PRIORITY);
        boolean saveResult = this.save(app);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建失败");
        }
        log.info("应用创建成功，ID: {}, 类型: {}", app.getId(), app.getCodeGenType());
        return app.getId();
    }

    @Override
    public boolean updateAppByUser(AppUpdateMyRequest appUpdateMyRequest, User loginUser) {
        if (appUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }
        Long id = appUpdateMyRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id 不能为空");
        }
        App app = this.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        if (!loginUser.getId().equals(app.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限");
        }
        String appName = appUpdateMyRequest.getAppName();
        if (StrUtil.isBlank(appName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用名称不能为空");
        }
        app.setAppName(appName);
        app.setEditTime(LocalDateTime.now());
        return this.updateById(app);
    }

    @Override
    public boolean deleteAppByUser(Long id, User loginUser) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id 不能为空");
        }
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }
        App app = this.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        if (!loginUser.getId().equals(app.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限");
        }
        return this.removeById(id);
    }

    @Override
    public boolean updateAppByAdmin(AppUpdateRequest appUpdateRequest) {
        if (appUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appUpdateRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id 不能为空");
        }
        App app = this.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        String appName = appUpdateRequest.getAppName();
        if (appName != null) {
            if (StrUtil.isBlank(appName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用名称不能为空");
            }
            app.setAppName(appName);
        }
        if (appUpdateRequest.getCover() != null) {
            app.setCover(appUpdateRequest.getCover());
        }
        if (appUpdateRequest.getPriority() != null) {
            app.setPriority(appUpdateRequest.getPriority());
        }
        app.setEditTime(LocalDateTime.now());
        return this.updateById(app);
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            appVO.setUser(userService.getUserVO(user));
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        return appList.stream()
                .map(this::getAppVO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<AppVO> getAppVOPage(Page<App> appPage) {
        if (appPage == null) {
            return null;
        }
        Page<AppVO> appVOPage = new Page<>(appPage.getPageNumber(), appPage.getPageSize(), appPage.getTotalRow());
        appVOPage.setTotalPage(appPage.getTotalPage());
        appVOPage.setRecords(getAppVOList(appPage.getRecords()));
        return appVOPage;
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public QueryWrapper getMyQueryWrapper(AppQueryMyRequest appQueryMyRequest, Long userId) {
        if (appQueryMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }
        Long id = appQueryMyRequest.getId();
        String appName = appQueryMyRequest.getAppName();
        String cover = appQueryMyRequest.getCover();
        String initPrompt = appQueryMyRequest.getInitPrompt();
        String codeGenType = appQueryMyRequest.getCodeGenType();
        String deployKey = appQueryMyRequest.getDeployKey();
        Integer priority = appQueryMyRequest.getPriority();
//        Long userId = appQueryMyRequest.getUserId();
        String sortField = appQueryMyRequest.getSortField();
        String sortOrder = appQueryMyRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .orderBy(sortField, "ascend".equals(sortOrder));
//        String appName = appQueryMyRequest.getAppName();
//        String sortField = appQueryMyRequest.getSortField();
//        String sortOrder = appQueryMyRequest.getSortOrder();
//        return QueryWrapper.create()
//                .eq("userId", userId)
//                .like("appName", appName)
//                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public QueryWrapper getGoodQueryWrapper(String appName) {
        return QueryWrapper.create()
                .ge("priority", AppConstant.GOOD_APP_PRIORITY)
                .like("appName", appName)
                .orderBy("priority", false);
    }
}
