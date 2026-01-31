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

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com"></a>
 */
public interface AppService extends IService<App> {

    long createApp(AppAddRequest appAddRequest, User loginUser);

    boolean updateAppByUser(AppUpdateMyRequest appUpdateMyRequest, User loginUser);

    boolean deleteAppByUser(Long id, User loginUser);

    boolean updateAppByAdmin(AppUpdateRequest appUpdateRequest);

    AppVO getAppVO(App app);

    List<AppVO> getAppVOList(List<App> appList);

    Page<AppVO> getAppVOPage(Page<App> appPage);

    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    QueryWrapper getMyQueryWrapper(AppQueryMyRequest appQueryMyRequest, Long userId);

    QueryWrapper getGoodQueryWrapper(String appName);
}
