package com.hanium.smart_drain.file.storage;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class LocalFileStorage implements FileStorage {

    @Override
    public String store(MultipartFile file) {
        // TODO: 실제 로컬 파일 저장 로직 구현 예정
        if (file == null || file.getOriginalFilename() == null) {
            return "local-path/unknown";
        }
        return "local-path/" + file.getOriginalFilename();
    }
}
