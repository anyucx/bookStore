package com.bookstore.storage;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StoredFile {
    private String originalName;
    private String storedName;
    private String relativePath;
    private String accessUrl;
    private String contentType;
    private long size;
}
