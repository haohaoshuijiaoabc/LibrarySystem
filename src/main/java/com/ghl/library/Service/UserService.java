package com.ghl.library.Service;

import com.ghl.library.DTO.UserLoginDTO;
import com.ghl.library.Pojo.Result;
import com.ghl.library.VO.UserLoginVO;
import jakarta.servlet.http.HttpSession;

/**
 * 用户业务接口。
 *
 * @author ghl
 */
public interface UserService {

    /**
     * 用户登录。
     *
     * @param userLoginDTO 登录参数
     * @param session      会话
     * @return 登录结果
     */
    Result<Void> login(UserLoginDTO userLoginDTO, HttpSession session);

    /**
     * 获取当前登录用户信息。
     *
     * @param session 会话
     * @return 用户信息
     */
    Result<UserLoginVO> getInfo(HttpSession session);

    /**
     * 用户退出登录。
     *
     * @param session 会话
     * @return 退出结果
     */
    Result<Void> logout(HttpSession session);
}
