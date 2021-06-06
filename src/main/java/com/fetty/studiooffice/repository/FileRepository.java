package com.fetty.studiooffice.repository;

import com.fetty.studiooffice.entity.file.File;
import com.fetty.studiooffice.entity.file.FileSrc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
}