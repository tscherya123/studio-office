package com.fetty.studiooffice.repository;

import com.fetty.studiooffice.entity.ticket.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
}