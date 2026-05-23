package com.hanium.smart_drain.file.service;

import com.hanium.smart_drain.file.repository.FileMetadataRepository;
import com.hanium.smart_drain.file.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileMetadataRepository fileMetadataRepository;
    private final FileStorage fileStorage;

    // TODO: MultipartFile 저장 로직 구현 예정
    // TODO: FileMetadata 저장 로직 구현 예정
    // TODO: 파일 URL 반환 로직 구현 예정
    // TODO: 추후 로컬 저장소에서 S3로 교체 가능하도록 설계
}
