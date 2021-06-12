package com.fetty.studiooffice.entity.data;

import com.fetty.studiooffice.entity.auth.User;
import com.fetty.studiooffice.entity.file.File;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommentResponse {
    private Long id;
    private FindUserResponse user;
    private String comment;
    private Set<File> files = new HashSet<>();
    private Date created;
}
