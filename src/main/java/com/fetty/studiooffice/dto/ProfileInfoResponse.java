package com.fetty.studiooffice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProfileInfoResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private String username;
    private String email;
    private Date birthDate;
    private String profileImg;
    private Date created;
}
