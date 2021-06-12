package com.fetty.studiooffice.repository;

import com.fetty.studiooffice.entity.ticket.ETicketStatus;
import com.fetty.studiooffice.entity.ticket.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketStatusRepository extends JpaRepository<TicketStatus, Long> {
    Optional<TicketStatus> findByCode(ETicketStatus ticketStatus);
}