package com.campustrade.platform.upload.service;

import com.campustrade.platform.common.AppException;
import com.campustrade.platform.config.AppProperties;
import com.campustrade.platform.upload.dto.response.UploadResponseDTO;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UploadService {

    private static final List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".webp");
    private static final DateTimeFormatter OBJECT_PREFIX_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM");

    private final MinioClient minioClient;
    private final AppProperties.Minio minioProperties;
    private volatile boolean bucketReady;

    public UploadService(MinioClient minioClient, AppProperties appProperties) {
        this.minioClient = minioClient;
        this.minioProperties = appProperties.getMinio();
    }

    public UploadResponseDTO storeImage(MultipartFile file) {
        validateImage(file);
        ensureBucketReady();

        String extension = getExtension(file.getOriginalFilename());
        String objectKey = buildObjectKey(extension);

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectKey)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(resolveContentType(file))
                            .build());
        } catch (Exception ex) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image", ex);
        }

        return new UploadResponseDTO(presignObjectKey(objectKey), objectKey);
    }

    private void ensureBucketReady() {
        if (bucketReady) {
            return;
        }

        synchronized (this) {
            if (bucketReady) {
                return;
            }

            try {
                boolean exists = minioClient.bucketExists(
                        BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build());
                if (!exists) {
                    if (!minioProperties.isAutoCreateBucket()) {
                        throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "MinIO bucket does not exist");
                    }
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucket()).build());
                }
                bucketReady = true;
            } catch (AppException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to prepare MinIO bucket", ex);
            }
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Please select an image to upload");
        }
        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only jpg/jpeg/png/webp images are supported");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Only image files can be uploaded");
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

    private String buildObjectKey(String extension) {
        String prefix = LocalDate.now(ZoneOffset.UTC).format(OBJECT_PREFIX_FORMATTER);
        String filename = UUID.randomUUID().toString().replace("-", "") + extension;
        return "images/" + prefix + "/" + filename;
    }

    private String resolveContentType(MultipartFile file) {
        return StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream";
    }

    private String buildPublicUrl(String objectKey) {
        String baseUrl = StringUtils.hasText(minioProperties.getPublicBaseUrl())
                ? minioProperties.getPublicBaseUrl().trim()
                : buildDefaultPublicBaseUrl();
        return trimTrailingSlash(baseUrl) + "/" + objectKey;
    }

    private String buildDefaultPublicBaseUrl() {
        String endpoint = minioProperties.getEndpoint().trim();
        String normalizedEndpoint;
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            normalizedEndpoint = endpoint;
        } else {
            normalizedEndpoint = (minioProperties.isSecure() ? "https://" : "http://") + endpoint;
        }
        return trimTrailingSlash(normalizedEndpoint) + "/" + minioProperties.getBucket();
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    public String presignUrl(String urlOrObjectKey) {
        if (!StringUtils.hasText(urlOrObjectKey)) {
            return urlOrObjectKey;
        }
        // Already a presigned URL - return as-is
        if (urlOrObjectKey.contains("X-Amz-")) {
            return urlOrObjectKey;
        }
        String objectKey = extractObjectKey(urlOrObjectKey);
        if (objectKey == null) {
            return urlOrObjectKey;
        }
        return presignObjectKey(objectKey);
    }

    public Map<String, String> presignUrls(List<String> urls) {
        Map<String, String> result = new HashMap<>();
        if (urls == null || urls.isEmpty()) {
            return result;
        }
        for (String url : urls) {
            result.put(url, presignUrl(url));
        }
        return result;
    }

    private String presignObjectKey(String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                    io.minio.GetPresignedObjectUrlArgs.builder()
                            .method(io.minio.http.Method.GET)
                            .bucket(minioProperties.getBucket())
                            .object(objectKey)
                            .expiry(minioProperties.getPresignExpiryDays(), TimeUnit.DAYS)
                            .build());
        } catch (Exception ex) {
            log.warn("Failed to presign object key: {}", objectKey, ex);
            return buildPublicUrl(objectKey);
        }
    }

    private String extractObjectKey(String urlOrObjectKey) {
        if (!StringUtils.hasText(urlOrObjectKey)) {
            return null;
        }
        // If it already looks like an object key (no scheme), return as-is
        if (!urlOrObjectKey.startsWith("http://") && !urlOrObjectKey.startsWith("https://")) {
            return urlOrObjectKey;
        }
        // Strip query parameters and fragment before matching
        String url = urlOrObjectKey;
        int queryIndex = url.indexOf('?');
        if (queryIndex > 0) {
            url = url.substring(0, queryIndex);
        }
        int fragmentIndex = url.indexOf('#');
        if (fragmentIndex > 0) {
            url = url.substring(0, fragmentIndex);
        }
        // Try to extract object key from the stored URL pattern: {baseUrl}/{bucket}/{objectKey}
        String baseUrl = buildDefaultPublicBaseUrl();
        String prefix = trimTrailingSlash(baseUrl) + "/";
        if (url.startsWith(prefix)) {
            return url.substring(prefix.length());
        }
        // Try publicBaseUrl
        if (StringUtils.hasText(minioProperties.getPublicBaseUrl())) {
            String publicPrefix = trimTrailingSlash(minioProperties.getPublicBaseUrl().trim()) + "/";
            if (url.startsWith(publicPrefix)) {
                return url.substring(publicPrefix.length());
            }
        }
        // Fallback: try to find bucket name in URL path
        String bucket = minioProperties.getBucket();
        String bucketSegment = "/" + bucket + "/";
        int bucketIndex = url.indexOf(bucketSegment);
        if (bucketIndex >= 0) {
            return url.substring(bucketIndex + bucketSegment.length());
        }
        return null;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UploadService.class);
}
