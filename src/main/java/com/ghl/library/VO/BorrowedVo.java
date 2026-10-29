package com.ghl.library.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BorrowedVo {
    private String username;
    private String bookname;
    private LocalDateTime borrow_time;
}
