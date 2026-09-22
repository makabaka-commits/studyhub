package com.studyhub.service.impl;

import com.studyhub.common.ErrorCode;
import com.studyhub.exception.BusinessException;
import com.studyhub.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp", "bmp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public String uploadAvatar(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "file is empty");
        }

        String extension = getExtension(file.getOriginalFilename());
        validateFile(file, extension);

        String filename = "avatar/" + UUID.randomUUID() + "." + extension;
        return saveFile(file, filename);
    }

    @Override
    public String uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "file is empty");
        }

        String extension = getExtension(file.getOriginalFilename());
        validateFile(file, extension);

        String filename = "images/" + UUID.randomUUID() + "." + extension;
        return saveFile(file, filename);
    }

    private String saveFile(MultipartFile file, String filename) {
        try {
            Path targetDir = Paths.get(uploadPath);
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            Path targetFile = Paths.get(uploadPath, filename);
            Path parentDir = targetFile.getParent();
            if (!Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + filename.replace("\\", "/");
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "file upload failed: " + e.getMessage());
        }
    }

    private String getExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "invalid file name");
        }
        return originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
    }

    private void validateFile(MultipartFile file, String extension) {
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR,
                    "unsupported file type: " + extension + ", allowed: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "file size exceeds 10MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "file content type must be an image");
        }

        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(12);
            if (!hasImageSignature(header)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "invalid image file content");
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "cannot inspect uploaded file");
        }
    }

    private boolean hasImageSignature(byte[] bytes) {
        if (bytes.length < 3) {
            return false;
        }
        boolean jpeg = (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff;
        boolean png = bytes.length >= 8 && (bytes[0] & 0xff) == 0x89 && bytes[1] == 0x50
                && bytes[2] == 0x4e && bytes[3] == 0x47;
        boolean gif = bytes.length >= 6 && bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F';
        boolean bmp = bytes[0] == 'B' && bytes[1] == 'M';
        boolean webp = bytes.length >= 12 && bytes[0] == 'R' && bytes[1] == 'I'
                && bytes[2] == 'F' && bytes[3] == 'F' && bytes[8] == 'W'
                && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
        return jpeg || png || gif || bmp || webp;
    }
}
