package com.boot.backend.ContactManager.services;

import org.springframework.web.multipart.MultipartFile;

public interface   ImageService {
    public String uploadFile(MultipartFile file);
}
