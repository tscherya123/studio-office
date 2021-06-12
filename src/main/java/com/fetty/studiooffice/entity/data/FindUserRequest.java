package com.fetty.studiooffice.entity.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FindUserRequest {
    @NotBlank
    private String searchQuery;

    private boolean admins;
    private boolean users;
    private boolean engineers;
}
