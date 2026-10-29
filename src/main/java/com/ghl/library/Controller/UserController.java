package com.ghl.library.Controller;

import com.ghl.library.Annotation.AuditLog;
import com.ghl.library.DTO.ReturnBookDTO;
import com.ghl.library.DTO.UserLoginDTO;
import com.ghl.library.Pojo.Book;
import com.ghl.library.Pojo.PageResult;
import com.ghl.library.Pojo.Result;
import com.ghl.library.Service.BookService;
import com.ghl.library.Service.BorrowService;
import com.ghl.library.Service.UserService;
import com.ghl.library.VO.BorrowedVo;
import com.ghl.library.VO.UserLoginVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 读者端接口。
 *
 * @author ghl
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    /** 默认页码 */
    private static final String DEFAULT_PAGE_NUM = "1";

    /** 默认每页条数 */
    private static final String DEFAULT_PAGE_SIZE = "10";

    @Resource
    private UserService userService;

    @Resource
    private BookService bookService;

    @Resource
    private BorrowService borrowService;

    @PostMapping("/login")
    public Result<Void> login(@RequestBody UserLoginDTO userLoginDTO, HttpSession session) {
        return userService.login(userLoginDTO, session);
    }

    @GetMapping("/info")
    public Result<UserLoginVO> getInfo(HttpSession session) {
        return userService.getInfo(session);
    }

    @GetMapping("/allbooks")
    public Result<List<Book>> getAllBooks(String name, String author) {
        return bookService.query(name, author);
    }

    @AuditLog(module = "借阅管理", action = "借阅图书")
    @PostMapping("/borrow")
    public Result<BorrowedVo> borrowBook(@RequestParam Integer bookId, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("login");
        return borrowService.borrow(userId, bookId);
    }

    @GetMapping("/borrowed")
    public Result<PageResult<BorrowedVo>> getBorrower(
            @RequestParam(defaultValue = DEFAULT_PAGE_NUM) Integer pageNum,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer pageSize,
            HttpSession session) {
        return borrowService.borrowed(pageNum, pageSize, session);
    }

    @AuditLog(module = "借阅管理", action = "归还图书")
    @PostMapping("/return")
    public Result<Void> returnBook(@RequestBody ReturnBookDTO bookDTO, HttpSession session) {
        return borrowService.returnBook(bookDTO.getName(), session);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        return userService.logout(session);
    }
}
