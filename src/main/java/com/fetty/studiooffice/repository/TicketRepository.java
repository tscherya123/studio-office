package com.fetty.studiooffice.repository;

import com.fetty.studiooffice.entity.ticket.Service;
import com.fetty.studiooffice.entity.ticket.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

}