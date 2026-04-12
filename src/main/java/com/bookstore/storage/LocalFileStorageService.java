package com.bookstore.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {
    @Value("${bookstore.file.base-path:uploads}")
    private String basePath;
    @Value("${bookstore.file.base-url-prefix:/uploads/}")
    private String urlPrefix;

    @Override
    public StoredFile store(MultipartFile file) throws IOException {
        String ext = "";
        String name = file.getOriginalFilename();
        if (StringUtils.hasText(name) && name.contains(".")) {
            ext = name.substring(name.lastIndexOf('.'));
        }
        String folder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String stored = UUID.randomUUID().toString().replace("-", "") + ext;
        Path dir = Paths.get(basePath).toAbsolutePath().normalize().resolve(folder);
        Files.createDirectories(dir);
        Path target = dir.resolve(stored);
        file.transferTo(target.toFile());
        StoredFile f = new StoredFile();
        f.originalName = name;
        f.storedName = stored;
        f.relativePath = folder + "/" + stored;
        f.accessUrl = (urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/") + f.relativePath.replace(File.separatorChar, '/');
        f.contentType = file.getContentType();
        f.size = file.getSize();
        return f;
    }
}
