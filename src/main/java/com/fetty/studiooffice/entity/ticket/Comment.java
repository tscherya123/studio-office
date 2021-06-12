package com.fetty.studiooffice.entity.ticket;

import com.fetty.studiooffice.entity.auth.User;
import com.fetty.studiooffice.entity.file.File;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "comment")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;

    @Column
    private String comment;

    @ElementCollection(targetClass = File.class)
    private Set<File> files = new HashSet<>();

    @Column
    private Date created;
}
