package com.fetty.studiooffice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdatePasswordRequest {
    private Long userId;
    private String oldPassword;
    private String newPassword;
}
