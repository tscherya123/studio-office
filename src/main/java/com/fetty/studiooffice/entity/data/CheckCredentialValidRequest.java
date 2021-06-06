package com.fetty.studiooffice.entity.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CheckCredentialValidRequest {
    @NotBlank
    @Size(max = 100)
    private String credential;

    @NotBlank
    //username, email, phone
    private String type;
}
