package com.ghl.library.Util;

public class Code {
    public static final int SUCCESS = 200;
    public static final String SUCCESS_MSG = "成功";

    public static final int PARAM_ERROR = 400;
    public static final String PARAM_ERROR_MSG = "请求参数格式不正确";

    public static final int INFO_EMPTY_ERROR = 300;
    public static final String INFO_EMPTY_ERROR_MSG = "输入信息不完整";

    public static final int USER_LOGIN_SESSION_EMPTY_ERROR = 1000;
    public static final String USER_LOGIN_SESSION_EMPTY_ERROR_MSG = "用户未登录";

    public static final int USER_OR_PASSWORD_ERROR = 1001;
    public static final String USER_OR_PASSWORD_ERROR_MSG = "用户名或密码错误";
    public static final String USER_PASSWORD_ERROR_MSG = "密码错误";

    public static final int USER_ERROR = 1002;
    public static final String USER_ERROR_MSG = "用户信息不存在";

    public static final int USER_FROZEN_ERROR = 1003;
    public static final String USER_FROZEN_ERROR_MSG = "用户已被封禁";


    public static final int BOOK_EXIST_ERROR = 4000;
    public static final String BOOK_EXIST_ERROR_MSG = "书本不存在";

    public static final int BOOK_BORROWED_ERROR = 4001;
    public static final String BOOK_BORROW_ERROR_MSG = "书本已借阅";

    public static final int BOOK_STOCK_ERROR = 4002;
    public static final String BOOK_STOCK_ERROR_MSG = "库存不足";


    public static final int BOOK_RETURNING = 4003;
    public static final String BOOK_RETURNING_MSG = "书本已归还";

    public static final int BOOK_RETURN_ERROR = 4004;
    public static final String BOOK_RETURNING_ERROR_MSG = "书本归还失败";

    public static final int BOOK_EXIST = 4005;
    public static final String BOOK_EXIST_MSG = "书本已存在";


    public static final int ADMIN_LOGIN_SESSION_EMPTY_ERROR = 5000;
    public static final String ADMIN_LOGIN_SESSION_EMPTY_ERROR_MSG = "管理员未登录";

    public static final int ADD_NUMBER_ERROR = 6000;
    public static final String ADD_NUMBER_ERROR_MSG = "添加库存数量应大于0";

    public static final int ADD_FAIL_ERROR = 6001;
    public static final String ADD_FAIL_ERROR_MSG = "添加库存失败";

    public static final int REDUCE_FAIL_ERROR = 6002;
    public static final String REDUCE_FAIL_ERROR_MSG = "删减库存失败";

    public static final int APPEND_BOOK_INFO_ERROR = 6003;
    public static final String APPEND_BOOK_INFO_ERROR_MSG = "书本信息不完整";

    public static final int APPEND_BOOK_ERROR = 6004;
    public static final String APPEND_BOOK_ERROR_MSG = "新增书本失败";

    public static final int BOOK_STOCK_EXIST = 6005;
    public static final String BOOK_STOCK_EXIST_MSG = "该图书还有剩余库存，请取出";

    public static final int DELETE_BOOK_ERROR = 6006;
    public static final String DELETE_BOOK_ERROR_MSG = "删除书本失败";


    public static final int UNKNOWN_CODE_ERROR = 9000;
    public static final String UNKNOWN_CODE_ERROR_MSG = "未知错误，请稍后重试";


    public static final int CHANGE_STATE_ERROR = 6007;
    public static final String CHANGE_STATE_ERROR_MSG = "更改状态失败";
}
