package com.bookstore.storage;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileStorageService {
    StoredFile store(MultipartFile file) throws IOException;
}
