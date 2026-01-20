package com.paul.appgen.service;

import com.mybatisflex.core.service.IService;
import com.paul.appgen.model.dto.user.UserLoginRequest;
import com.paul.appgen.model.entity.User;
import com.paul.appgen.model.vo.LoginUserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.net.http.HttpRequest;

/**
 * 用户 服务层。
 *
 * @author <a href="https://github.com"></a>
 */
public interface UserService extends IService<User> {
    //用户注册
    long userRegister(String userAccount, String userPassword,String checkPassword);

    //密码加密
    String getEncryptPassword(String password);

    //获取用户登录信息
    LoginUserVO getLoginUserVO(User user);

    //用户登录
    LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);

    //获取用户登录信息
    User getLoginUser(HttpServletRequest httpServletRequest);

    boolean userLogout(HttpServletRequest httpServletRequest);

}
