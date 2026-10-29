package com.ghl.library.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionBookNumbersDTO {
    private String book;
    private Integer numbers;
}
