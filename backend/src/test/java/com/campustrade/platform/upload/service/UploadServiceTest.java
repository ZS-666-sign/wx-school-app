package com.campustrade.platform.upload.service;

import com.campustrade.platform.common.AppException;
import com.campustrade.platform.config.AppProperties;
import com.campustrade.platform.upload.dto.response.UploadResponseDTO;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadServiceTest {

    @Mock
    private MinioClient minioClient;

    private UploadService uploadService;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties();
        AppProperties.Minio minio = new AppProperties.Minio();
        minio.setEndpoint("http://127.0.0.1:9000");
        minio.setAccessKey("test-access");
        minio.setSecretKey("test-secret");
        minio.setBucket("test-bucket");
        minio.setPublicBaseUrl("http://cdn.example.com/test-bucket");
        minio.setSecure(false);
        minio.setAutoCreateBucket(true);
        properties.setMinio(minio);

        uploadService = new UploadService(minioClient, properties);
    }

    @Test
    void storeImageUploadsToMinio() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "image-content".getBytes());

        when(minioClient.bucketExists(any())).thenReturn(true);
        when(minioClient.putObject(any())).thenReturn((ObjectWriteResponse) null);
        when(minioClient.getPresignedObjectUrl(any())).thenReturn("http://127.0.0.1:9000/test-bucket/images/presigned?token=abc");

        UploadResponseDTO response = uploadService.storeImage(file);

        assertEquals("http://127.0.0.1:9000/test-bucket/images/presigned?token=abc", response.url());
        assertTrue(response.filename().startsWith("images/"));
        assertTrue(response.filename().endsWith(".png"));
        verify(minioClient).bucketExists(any());
        verify(minioClient).putObject(any());
        verify(minioClient, never()).makeBucket(any());
    }

    @Test
    void storeImageCreatesBucketWhenMissing() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "goods.jpg",
                "image/jpeg",
                "image-content".getBytes());

        when(minioClient.bucketExists(any())).thenReturn(false);
        when(minioClient.putObject(any())).thenReturn((ObjectWriteResponse) null);
        when(minioClient.getPresignedObjectUrl(any())).thenReturn("http://127.0.0.1:9000/test-bucket/images/presigned?token=abc");

        uploadService.storeImage(file);

        verify(minioClient).bucketExists(any());
        verify(minioClient).makeBucket(any());
        verify(minioClient).putObject(any());
    }

    @Test
    void storeImageRejectsInvalidExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.txt",
                "text/plain",
                "hello".getBytes());

        AppException exception = assertThrows(AppException.class, () -> uploadService.storeImage(file));

        assertEquals("Only jpg/jpeg/png/webp images are supported", exception.getMessage());
        verify(minioClient, never()).putObject(any());
    }

    @Test
    void storeImageRejectsEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                new byte[0]);

        AppException exception = assertThrows(AppException.class, () -> uploadService.storeImage(file));

        assertEquals("Please select an image to upload", exception.getMessage());
        verify(minioClient, never()).putObject(any());
    }
}
