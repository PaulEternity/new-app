package com.paul.appgen.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.paul.appgen.constant.AppConstant;
import com.paul.appgen.core.AiCodeGeneratorFacade;
import com.paul.appgen.exception.BusinessException;
import com.paul.appgen.exception.ErrorCode;
import com.paul.appgen.exception.ThrowUtils;
import com.paul.appgen.mapper.AppMapper;
import com.paul.appgen.model.dto.app.AppAddRequest;
import com.paul.appgen.model.dto.app.AppQueryMyRequest;
import com.paul.appgen.model.dto.app.AppQueryRequest;
import com.paul.appgen.model.dto.app.AppUpdateMyRequest;
import com.paul.appgen.model.dto.app.AppUpdateRequest;
import com.paul.appgen.model.entity.App;
import com.paul.appgen.model.entity.User;
import com.paul.appgen.model.enums.CodeGenTypeEnum;
import com.paul.appgen.model.vo.AppVO;
import com.paul.appgen.service.AppService;
import com.paul.appgen.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
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

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;


/**
 * 处理用户生成代码的请求
 * 这是一个重写的方法，用于处理用户与应用程序交互以生成代码的请求
 *
 * @param appId 应用程序的唯一标识符
 * @param message 用户输入的消息内容
 * @param loginUser 当前登录用户的信息
 * @return 返回一个Flux<String>类型的响应流，包含生成的代码片段
 * @throws BusinessException 当请求参数无效或用户无权限时抛出业务异常
 */
    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
    // 验证请求参数的有效性，确保appId大于0，message和loginUser不为空
        if (appId <= 0 || message == null || loginUser == null) {
            return Flux.error(new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空"));
        }
    // 根据appId获取应用程序信息
        App app = this.getById(appId);
    // 如果应用程序不存在，则抛出异常
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
    // 验证当前用户是否有权限访问该应用程序
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限");
        }
    // 获取应用程序的代码生成类型
        String codeGenType = app.getCodeGenType();
    // 将代码生成类型字符串转换为对应的枚举值
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
    // 如果代码生成类型无效，则抛出异常
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码生成类型错误");
        }
    // 调用AI代码生成门面，生成并保存代码，返回代码片段流
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
    }

/**
 * 创建应用的方法
 * @param appAddRequest 应用添加请求参数
 * @param loginUser 当前登录用户信息
 * @return 新创建的应用ID
 * @throws BusinessException 当参数为空、用户未登录、initPrompt为空、代码生成类型不支持或创建失败时抛出
 */
    @Override
    public long createApp(AppAddRequest appAddRequest, User loginUser) {
    // 检查请求参数是否为空
        if (appAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
    // 检查用户是否已登录
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }
    // 获取并检查初始化提示词是否为空
        String initPrompt = appAddRequest.getInitPrompt();
        if (StrUtil.isBlank(initPrompt)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "initPrompt 不能为空");
        }
    // 创建新应用对象
        App app = new App();
    // 设置应用名称，如果为空则使用默认名称"未命名应用"
        String appName = appAddRequest.getAppName();
        app.setAppName(StrUtil.isBlank(appName) ? "未命名应用" : appName);
    // 设置应用封面
        app.setCover(appAddRequest.getCover());
    // 设置初始化提示词
        app.setInitPrompt(initPrompt);
    // 检查并设置代码生成类型
        String codeGenType = appAddRequest.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的代码生成类型");
        }
        app.setCodeGenType(codeGenType);
        app.setUserId(loginUser.getId());
        app.setPriority(AppConstant.DEFAULT_APP_PRIORITY);
        boolean saveResult = this.save(app);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建失败");
        }
        log.info("应用创建成功，ID: {}, 类型: {}", app.getId(), app.getCodeGenType());
        return app.getId();
    }

/**
 * 部署应用方法
 * @param appId 应用ID
 * @param loginUser 登录用户信息
 * @return 返回部署后的访问URL
 * TODO:暂时不支持版本，最多保留一个当前版本
 */
    @Override
    public String deployApp(Long appId, User loginUser) {
    // 校验appId参数是否有效
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "appId 不能为空");
    // 根据appId获取应用信息
        App app = this.getById(appId);
    // 校验应用是否存在
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
    // 校验用户是否有权限部署该应用
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限");
        }
    // 获取或生成部署密钥
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
    // 构建代码生成目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        File sourceDir = new File(sourceDirPath);
    // 校验代码生成目录是否存在
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "代码生成目录不存在，请先生成应用");
        }
    // 构建部署目录路径并复制代码文件
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "部署失败 : " + e.getMessage());
        }
    // 更新应用的部署信息
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
    // 校验更新是否成功
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败！");
    // 返回部署后的访问URL
        return String.format("%s/%s", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }

/**
 * 根据用户请求更新应用信息
 * @param appUpdateMyRequest 应用更新请求参数，包含要更新的应用信息
 * @param loginUser 当前登录用户信息
 * @return 更新成功返回true，否则返回false
 * @throws BusinessException 当参数校验失败或权限不足时抛出业务异常
 */
    @Override
    public boolean updateAppByUser(AppUpdateMyRequest appUpdateMyRequest, User loginUser) {
    // 检查请求参数是否为空
        if (appUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
    // 检查用户是否登录
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }
    // 获取应用ID并校验
        Long id = appUpdateMyRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id 不能为空");
        }
    // 根据ID查询应用是否存在
        App app = this.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
    // 校验当前用户是否有权限修改该应用
        if (!loginUser.getId().equals(app.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限");
        }
    // 获取应用名称并校验
        String appName = appUpdateMyRequest.getAppName();
        if (StrUtil.isBlank(appName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用名称不能为空");
        }
    // 设置新的应用名称和编辑时间
        app.setAppName(appName);
        app.setEditTime(LocalDateTime.now());
    // 执行更新操作并返回结果
        return this.updateById(app);
    }

/**
 * 根据用户ID删除应用
 * @param id 应用ID
 * @param loginUser 当前登录用户信息
 * @return 删除是否成功
 * @throws BusinessException 当参数无效、用户未登录、应用不存在或无权限时抛出业务异常
 */
    @Override
    public boolean deleteAppByUser(Long id, User loginUser) {
    // 检查ID是否为空或小于等于0
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id 不能为空");
        }
    // 检查用户是否登录
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }
    // 根据ID获取应用信息
        App app = this.getById(id);
    // 检查应用是否存在
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
    // 检查当前用户是否有权限删除该应用（只有应用创建者可以删除）
        if (!loginUser.getId().equals(app.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限");
        }
    // 执行删除操作并返回结果
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
