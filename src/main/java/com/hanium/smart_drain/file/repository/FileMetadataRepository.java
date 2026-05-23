package com.hanium.smart_drain.file.repository;

import com.hanium.smart_drain.file.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
}
