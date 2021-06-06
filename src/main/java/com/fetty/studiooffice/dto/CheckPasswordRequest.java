package com.fetty.studiooffice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CheckPasswordRequest {
    private Long userId;
    private String password;
}
