package com.guruge.hardware.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class FileStorageConfig {

    private final AppProperties appProperties;

    @Bean
    CommandLineRunner createUploadDirs() {
        return args -> {
            createDirIfMissing(appProperties.getStorage().getLocation());
            createDirIfMissing(appProperties.getStorage().getProductImages());
        };
    }

    private void createDirIfMissing(String dir) throws Exception {
        if (dir == null || dir.isBlank()) {
            return;
        }
        Path path = Paths.get(dir).toAbsolutePath().normalize();
        Files.createDirectories(path);
    }
}
