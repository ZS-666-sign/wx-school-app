package com.campustrade.platform.upload.service;

import com.campustrade.platform.common.AppException;
import com.campustrade.platform.config.AppProperties;
import com.campustrade.platform.upload.dto.response.UploadResponseDTO;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class UploadService {

    private static final List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".webp", ".gif");
    private final Path uploadRoot;

    public UploadService(AppProperties appProperties) {
        this.uploadRoot = Paths.get(appProperties.getUpload().getDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadRoot);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to create upload directory", ex);
        }
    }

    public UploadResponseDTO storeImage(MultipartFile file) {
        validateImage(file);
        String extension = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString().replace("-", "") + extension;
        Path target = uploadRoot.resolve(filename).normalize();
        ensureInUploadDir(target);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "图片上传失败");
        }

        String url = "/api/v1/uploads/" + filename;
        return new UploadResponseDTO(url, filename);
    }

    public Resource loadAsResource(String filename) {
        if (!StringUtils.hasText(filename)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "文件名不能为空");
        }
        Path target = uploadRoot.resolve(filename).normalize();
        ensureInUploadDir(target);
        if (!Files.exists(target) || !Files.isRegularFile(target)) {
            throw new AppException(HttpStatus.NOT_FOUND, "文件不存在");
        }
        try {
            return new UrlResource(target.toUri());
        } catch (IOException ex) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "读取文件失败");
        }
    }

    public String detectContentType(String filename) {
        Path target = uploadRoot.resolve(filename).normalize();
        ensureInUploadDir(target);
        try {
            String detected = Files.probeContentType(target);
            return StringUtils.hasText(detected) ? detected : "application/octet-stream";
        } catch (IOException ex) {
            return "application/octet-stream";
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "请选择要上传的图片");
        }
        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "仅支持 jpg/jpeg/png/webp/gif 格式图片");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new AppException(HttpStatus.BAD_REQUEST, "仅允许上传图片文件");
        }
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return ".jpg";
        }
        String lower = filename.toLowerCase(Locale.ROOT);
        int index = lower.lastIndexOf('.');
        if (index < 0) {
            return ".jpg";
        }
        return lower.substring(index);
    }

    private void ensureInUploadDir(Path path) {
        if (!path.startsWith(uploadRoot)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "非法文件路径");
        }
    }
}
