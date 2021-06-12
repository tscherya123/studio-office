package com.fetty.studiooffice.entity.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FindUserResponse {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String imgLink;
}
