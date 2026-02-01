package com.paul.appgen.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.paul.appgen.model.dto.app.AppAddRequest;
import com.paul.appgen.model.dto.app.AppQueryMyRequest;
import com.paul.appgen.model.dto.app.AppQueryRequest;
import com.paul.appgen.model.dto.app.AppUpdateMyRequest;
import com.paul.appgen.model.dto.app.AppUpdateRequest;
import com.paul.appgen.model.entity.App;
import com.paul.appgen.model.entity.User;
import com.paul.appgen.model.vo.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com"></a>
 */
public interface AppService extends IService<App> {

/**
 * 创建应用程序的方法
 *
 * @param appAddRequest 包含添加应用程序所需信息的请求对象
 * @param loginUser 执行创建操作的用户登录信息
 * @return 返回创建成功后的应用程序ID（长整型）
 */
    long createApp(AppAddRequest appAddRequest, User loginUser);

/**
 * 部署应用的方法
 * @param appId 应用ID，用于标识需要部署的应用
 * @param loginUser 登录用户信息，包含当前登录用户的权限和相关信息
 * @return 返回一个String类型的结果，可访问的部署地址
 */
    String deployApp(Long appId,User loginUser);


/**
 * 根据用户请求更新应用程序信息
 *
 * @param appUpdateMyRequest 包含应用程序更新信息的请求对象
 * @param loginUser 当前登录用户信息
 * @return 更新操作是否成功执行，返回true表示成功，false表示失败
 */
    boolean updateAppByUser(AppUpdateMyRequest appUpdateMyRequest, User loginUser);

/**
 * 根据用户ID和登录用户信息删除应用
 *
 * @param id 要删除的应用ID
 * @param loginUser 当前登录用户信息
 * @return 删除操作是否成功执行
 */
    boolean deleteAppByUser(Long id, User loginUser);

/**
 * 管理员更新应用信息的方法
 * @param appUpdateRequest 包含应用更新信息的请求对象，其中包含了需要更新的应用相关信息
 * @return 更新操作是否成功，返回true表示更新成功，返回false表示更新失败
 */
    boolean updateAppByAdmin(AppUpdateRequest appUpdateRequest);

/**
 * 根据App实体对象获取对应的AppVO对象
 * AppVO通常用于数据传输，可能包含与App实体不同或额外的字段
 *
 * @param app App实体对象，包含应用的基本信息
 * @return AppVO 视图对象，用于前端展示或数据传输
 */
    AppVO getAppVO(App app);

/**
 * 获取应用视图对象(AppVO)列表
 * 该方法将应用实体列表转换为视图对象列表，通常用于数据展示层
 *
 * @param appList 应用实体列表，包含完整的应用信息
 * @return 视图对象列表，包含用于展示的应用信息
 */
    List<AppVO> getAppVOList(List<App> appList);

/**
 * 获取应用视图对象(AppVO)的分页列表
 * 该方法接收一个应用实体(App)的分页对象，并返回一个应用视图对象(AppVO)的分页列表
 *
 * @param appPage 应用实体(App)的分页对象，包含分页信息和应用实体数据
 * @return 返回一个应用视图对象(AppVO)的分页列表，包含分页信息和视图对象数据
 */
    Page<AppVO> getAppVOPage(Page<App> appPage);

/**
 * 根据查询请求条件构建QueryWrapper对象
 *
 * 该方法用于将查询请求参数转换为MyBatis-Plus的QueryWrapper对象，
 * 以便构建数据库查询条件。QueryWrapper提供了一种灵活的方式来
 * 动态构建SQL查询语句，支持链式调用。
 *
 * @param appQueryRequest 查询请求参数对象，包含查询条件
 * @return 返回构建好的QueryWrapper对象，用于数据库查询条件封装
 */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

/**
 * 根据请求参数和用户ID生成查询条件包装器
 *
 * @param appQueryMyRequest 查询请求对象，包含查询条件参数
 * @param userId 用户ID，用于筛选特定用户的数据
 * @return QueryWrapper 返回包含查询条件的MyBatis-Plus查询包装器对象
 */
    QueryWrapper getMyQueryWrapper(AppQueryMyRequest appQueryMyRequest, Long userId);

/**
 * 获取商品查询条件构造器
 * @param appName 应用名称
 * @return QueryWrapper 商品查询条件构造器，用于构建数据库查询条件
 */
    QueryWrapper getGoodQueryWrapper(String appName);

/**
 * 根据应用ID、用户消息和登录用户信息生成代码的响应式流
 *
 * @param appId 应用程序的唯一标识符
 * @param message 用户输入的消息内容
 * @param loginUser 当前登录的用户信息
 * @return 返回一个包含生成代码的Flux流，Flux是Reactive编程中的响应式流类型
 */
    Flux<String> chatToGenCode(Long appId, String message,User loginUser);
}
