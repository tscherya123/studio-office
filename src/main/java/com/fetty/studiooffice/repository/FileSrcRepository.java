package com.fetty.studiooffice.repository;

import com.fetty.studiooffice.entity.file.EFileSrc;
import com.fetty.studiooffice.entity.file.FileSrc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileSrcRepository extends JpaRepository<FileSrc, Long> {
    Optional<FileSrc> findByCode(EFileSrc fileSrc);
}