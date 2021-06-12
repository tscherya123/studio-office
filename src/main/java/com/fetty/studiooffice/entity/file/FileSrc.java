package com.fetty.studiooffice.entity.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "file_src")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FileSrc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 200)
    private EFileSrc code;
}
