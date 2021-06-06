package com.fetty.studiooffice.security.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fetty.studiooffice.entity.auth.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UserDetailsInfo implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String firstName;

    private String lastName;

    private String username;

    private String email;

    private String phone;

    @JsonIgnore
    private String password;

    private boolean banned;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsInfo(Long id, String firstName, String lastName, String username, String email,
                           String phone, String password, boolean banned, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.banned = banned;
        this.authorities = authorities;
    }

    public static UserDetailsInfo build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getCode().name()))
                .collect(Collectors.toList());

        return new UserDetailsInfo(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getPassword(),
                user.isBanned(),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsInfo user = (UserDetailsInfo) o;
        return Objects.equals(id, user.id);
    }
}
