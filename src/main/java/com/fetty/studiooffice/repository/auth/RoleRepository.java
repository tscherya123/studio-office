package com.fetty.studiooffice.repository.auth;


import com.fetty.studiooffice.entity.auth.ERole;
import com.fetty.studiooffice.entity.auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCode(ERole code);
}