package com.g4.ems.service;

import com.g4.ems.exception.BusinessValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;

@Service
public class BillStorageService {

    private final Path billsDirectory;

    public BillStorageService(@Value("${app.bill-upload-dir:uploads\\bills}") String uploadDir) {
        this.billsDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessValidationException("Bill file is required.");
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String extension = getExtension(originalName);
        String savedName = Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + extension;

        try {
            Files.createDirectories(billsDirectory);
            Path destination = billsDirectory.resolve(savedName).normalize();
            file.transferTo(destination);
            return "/api/v1/bills/" + savedName;
        } catch (IOException ex) {
            throw new BusinessValidationException("Failed to store bill file.");
        }
    }

    public Resource loadAsResource(String filename) {
        Path filePath = billsDirectory.resolve(filename).normalize();
        if (!filePath.startsWith(billsDirectory)) {
            throw new BusinessValidationException("Invalid bill file path.");
        }
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessValidationException("Bill file not found.");
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new BusinessValidationException("Invalid bill file reference.");
        }
    }

    private String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0) {
            return "";
        }
        return fileName.substring(index);
    }
}
