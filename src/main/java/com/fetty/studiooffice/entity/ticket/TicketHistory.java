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
@Table(name = "ticket_history")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TicketHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date modified;

    private String prevStatus;
    private String newStatus;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;
}
