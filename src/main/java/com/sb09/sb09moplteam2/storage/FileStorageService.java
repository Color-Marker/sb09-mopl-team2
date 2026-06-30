package com.sb09.sb09moplteam2.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

  String store(MultipartFile file);
}