package com.fetty.studiooffice.entity.file;

import com.fetty.studiooffice.entity.auth.User;
import com.fetty.studiooffice.entity.ticket.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "file")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 300)
    private String name;
    @Column(length = 1000)
    private String url;
    @Column
    private Long size;
    @ManyToOne
    @JoinColumn(name = "id_source")
    private FileSrc source;
    @Column(name = "content_type")
    private String contentType;
}
