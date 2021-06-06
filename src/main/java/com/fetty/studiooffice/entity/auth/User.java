package com.fetty.studiooffice.entity.auth;

import com.fetty.studiooffice.entity.file.File;
import com.fetty.studiooffice.entity.ticket.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "phone")
})
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    private String firstName;

    @NotBlank
    @Size(max = 200)
    private String lastName;

    @NotBlank
    @Size(max = 100)
    private String username;

    @NotBlank
    @Size(max = 100)
    @Email
    private String email;

    @NotBlank
    @Size(max = 25)
    private String phone;

    @NotBlank
    @Size(max = 300)
    private String password;

    @Column(name = "birth")
    private Date birthDate;

    @Column(name = "created")
    private Date accountCreated;

    @Column(name = "banned")
    private boolean banned;

    @ManyToOne
    @JoinColumn(name = "id_profile_img")
    private File profileImg;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(	name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public User(@NotBlank @Size(max = 200) String firstName, @NotBlank @Size(max = 200) String lastName, @NotBlank @Size(max = 100) String username, @NotBlank @Size(max = 100) @Email String email, @NotBlank @Size(max = 25) String phone, @NotBlank @Size(max = 300) String password, Date accountCreated) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.accountCreated = accountCreated;
    }
}
