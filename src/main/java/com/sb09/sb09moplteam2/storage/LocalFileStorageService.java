package com.sb09.sb09moplteam2.storage;

import com.sb09.sb09moplteam2.config.StorageProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mopl.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

  private final StorageProperties storageProperties;

  @Override
  public String store(MultipartFile file) {
    try {
      Path rootPath = Path.of(storageProperties.getLocal().getRootPath());
      Files.createDirectories(rootPath);

      String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
      Path targetPath = rootPath.resolve(filename);
      file.transferTo(targetPath);

      return "/files/" + filename;
    } catch (IOException e) {
      throw new IllegalStateException("파일 저장에 실패했습니다.", e);
    }
  }
}