package com.fetty.studiooffice.entity.ticket;

import com.fetty.studiooffice.entity.auth.Role;
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
@Table(name = "ticket")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_client")
    private User client;

    @ManyToOne
    @JoinColumn(name = "id_engineer")
    private User engineer;

//    @ManyToOne
//    @JoinColumn(name = "id_service")
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_service", referencedColumnName = "id")
    private Service service;

    @ManyToOne
    @JoinColumn(name = "id_status")
    private TicketStatus ticketStatus;

    @Column
    private Integer grnPrice;

    @Column
    private boolean payed;

    @Column
    private Integer returnCount;

    @Column
    private Date created;

    @Column
    private Date lastModified;

    @Column
    private String name;

    @Column
    private String description;

    @ElementCollection(targetClass = File.class)
    private Set<File> files = new HashSet<>();

    @ElementCollection(targetClass = Comment.class)
    private Set<Comment> comments = new HashSet<>();
}
