package com.fetty.studiooffice.entity.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class GetUsersResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Boolean banned;
    private Date birthDate;
    private String imgLink;
    private Integer ticketCount;
    private Integer bringMoney;
    private Date created;
}
