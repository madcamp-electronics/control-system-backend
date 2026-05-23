package com.hanium.smart_drain.file.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

    String store(MultipartFile file);
}
