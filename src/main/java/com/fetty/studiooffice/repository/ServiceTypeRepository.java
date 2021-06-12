package com.fetty.studiooffice.repository;

import com.fetty.studiooffice.entity.ticket.EServiceType;
import com.fetty.studiooffice.entity.ticket.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceTypeRepository extends JpaRepository<ServiceType, Long> {
    Optional<ServiceType> findByCode(EServiceType serviceType);
}