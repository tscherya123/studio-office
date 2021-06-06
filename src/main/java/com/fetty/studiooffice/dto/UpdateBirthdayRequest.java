package com.fetty.studiooffice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateBirthdayRequest {
    private Long userId;
    private Date date;
}
