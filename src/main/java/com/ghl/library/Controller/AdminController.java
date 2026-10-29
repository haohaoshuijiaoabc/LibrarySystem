package com.ghl.library.Controller;

import com.ghl.library.Annotation.AuditLog;
import com.ghl.library.DTO.AdminLoginDTO;
import com.ghl.library.DTO.AppendBookDTO;
import com.ghl.library.DTO.TransactionBookNumbersDTO;
import com.ghl.library.Pojo.Book;
import com.ghl.library.Pojo.OperationLog;
import com.ghl.library.Pojo.PageResult;
import com.ghl.library.Pojo.Result;
import com.ghl.library.Service.AdminService;
import com.ghl.library.Service.OperationLogService;
import com.ghl.library.Service.StatisticsService;
import com.ghl.library.VO.BookStatisticsVO;
import com.ghl.library.VO.UserVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员端接口。
 *
 * <p>写操作统一标注 {@link AuditLog}，由 AOP 异步记录操作日志。</p>
 *
 * @author ghl
 */
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    /** 默认页码 */
    private static final String DEFAULT_PAGE_NUM = "1";

    /** 默认每页条数 */
    private static final String DEFAULT_PAGE_SIZE = "10";

    @Resource
    private AdminService adminService;

    @Resource
    private StatisticsService statisticsService;

    @Resource
    private OperationLogService operationLogService;

    @PostMapping("/login")
    public Result<Void> login(@RequestBody AdminLoginDTO login, HttpSession session) {
        return adminService.login(login, session);
    }

    @GetMapping("/BookInfo")
    public Result<PageResult<Book>> getBookInfo(
            @RequestParam(defaultValue = DEFAULT_PAGE_NUM) Integer pageNum,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer pageSize,
            @RequestParam String bookName,
            @RequestParam String author) {
        return adminService.getBookInfo(pageNum, pageSize, bookName, author);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        return adminService.logout(session);
    }

    @AuditLog(module = "图书管理", action = "增加库存")
    @PostMapping("/addBooks")
    public Result<Void> addNumbers(@RequestBody TransactionBookNumbersDTO add) {
        return adminService.add(add);
    }

    @AuditLog(module = "图书管理", action = "扣减库存")
    @PostMapping("/reduceBooks")
    public Result<Void> reduceNumbers(@RequestBody TransactionBookNumbersDTO reduce) {
        return adminService.reduce(reduce);
    }

    @AuditLog(module = "图书管理", action = "新增图书")
    @PostMapping("/appendBooks")
    public Result<Void> append(@RequestBody AppendBookDTO book) {
        return adminService.append(book);
    }

    @AuditLog(module = "图书管理", action = "删除图书")
    @PostMapping("/deleteBooks")
    public Result<Void> deleteBooks(@RequestParam String name) {
        return adminService.delete(name);
    }

    @PostMapping("/getUserInfo")
    public Result<PageResult<UserVO>> getUserInfo(
            @RequestParam(defaultValue = DEFAULT_PAGE_NUM) Integer pageNum,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer pageSize,
            String userName,
            String phone) {
        return adminService.getUserInfo(pageNum, pageSize, userName, phone);
    }

    @AuditLog(module = "用户管理", action = "变更用户状态")
    @PostMapping("/changeState")
    public Result<Void> changeState(@RequestParam String name) {
        return adminService.changeState(name);
    }

    /**
     * 管理端数据看板统计。
     *
     * @return 统计数据
     */
    @GetMapping("/statistics")
    public Result<BookStatisticsVO> statistics() {
        return Result.success(statisticsService.getStatistics());
    }

    /**
     * 分页查询操作日志。
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 操作日志分页数据
     */
    @GetMapping("/operationLogs")
    public Result<PageResult<OperationLog>> operationLogs(
            @RequestParam(defaultValue = DEFAULT_PAGE_NUM) Integer pageNum,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer pageSize) {
        return Result.success(operationLogService.page(pageNum, pageSize));
    }
}
