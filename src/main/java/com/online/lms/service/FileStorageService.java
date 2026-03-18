package com.online.lms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService() {
        this.rootLocation = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation.resolve("videos"));
            Files.createDirectories(rootLocation.resolve("pdfs"));
            Files.createDirectories(rootLocation.resolve("docx"));
            Files.createDirectories(rootLocation.resolve("images"));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directories", e);
        }
    }

    /**
     * Store file and return path for serving (e.g. /uploads/videos/xxx.mp4).
     */
    public String store(MultipartFile file, String subFolder) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String ext = "";
            int dotIdx = originalFilename.lastIndexOf('.');
            if (dotIdx >= 0) {
                ext = originalFilename.substring(dotIdx);
            }
            String filename = UUID.randomUUID() + ext;
            Path destination = rootLocation.resolve(subFolder).resolve(filename);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            String relativePath = "/uploads/" + subFolder + "/" + filename;
            log.info("Stored file: {}", relativePath);
            return relativePath;
        } catch (IOException e) {
            throw new RuntimeException("Could not store file: " + file.getOriginalFilename(), e);
        }
    }
}
