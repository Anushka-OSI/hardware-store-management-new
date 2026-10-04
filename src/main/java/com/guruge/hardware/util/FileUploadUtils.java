package com.guruge.hardware.util;

import com.guruge.hardware.config.AppProperties;
import com.guruge.hardware.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FileUploadUtils {

    private final AppProperties appProperties;

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File is empty");
        }
        if (file.getSize() > appProperties.getStorage().getMaxFileSize()) {
            throw new BusinessException("File exceeds max size of "
                    + appProperties.getStorage().getMaxFileSize() + " bytes");
        }
        List<String> allowed = appProperties.getStorage().getAllowedTypes();
        String contentType = file.getContentType();
        if (allowed != null && !allowed.isEmpty()
                && (contentType == null || !allowed.contains(contentType))) {
            throw new BusinessException("File type not allowed: " + contentType);
        }
    }

    public String generateFilename(String originalFilename) {
        String ext = StringUtils.getFilenameExtension(originalFilename);
        String base = UUID.randomUUID().toString();
        return ext != null && !ext.isBlank() ? base + "." + ext : base;
    }

    /**
     * Validates, generates a safe UUID filename and saves under
     * {@code <storage.location>/<subDir>}. Returns the relative path
     * (e.g. {@code product-images/uuid.jpg}).
     */
    public String save(MultipartFile file, String subDir) throws IOException {
        validate(file);
        String baseDir = appProperties.getStorage().getLocation();
        Path targetDir = (subDir == null || subDir.isBlank())
                ? Paths.get(baseDir).toAbsolutePath().normalize()
                : Paths.get(baseDir, subDir).toAbsolutePath().normalize();
        Files.createDirectories(targetDir);
        String filename = generateFilename(file.getOriginalFilename());
        Path target = targetDir.resolve(filename).normalize();
        if (!target.startsWith(targetDir)) {
            throw new BusinessException("Invalid file path");
        }
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        String prefix = (subDir == null || subDir.isBlank()) ? "" : subDir + "/";
        return prefix + filename;
    }

    public boolean delete(String relativeOrAbsolutePath) {
        if (relativeOrAbsolutePath == null || relativeOrAbsolutePath.isBlank()) {
            return false;
        }
        try {
            Path path = Paths.get(relativeOrAbsolutePath);
            if (!path.isAbsolute()) {
                path = Paths.get(appProperties.getStorage().getLocation())
                        .resolve(relativeOrAbsolutePath).toAbsolutePath().normalize();
            }
            return Files.deleteIfExists(path);
        } catch (IOException ex) {
            return false;
        }
    }
}
