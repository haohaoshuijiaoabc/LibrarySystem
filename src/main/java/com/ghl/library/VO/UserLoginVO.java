package com.ghl.library.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginVO {
    private Integer id;
    private String username;
    private String phone;
}
