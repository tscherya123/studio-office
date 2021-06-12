package com.fetty.studiooffice.repository;

import com.fetty.studiooffice.entity.ticket.Comment;
import com.fetty.studiooffice.entity.ticket.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

}