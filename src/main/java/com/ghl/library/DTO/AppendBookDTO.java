package com.ghl.library.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppendBookDTO {
    private String bookName;
    private String author;
    private Integer stock;
}
