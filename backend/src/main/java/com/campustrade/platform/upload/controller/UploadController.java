package com.campustrade.platform.upload.controller;

import com.campustrade.platform.upload.dto.response.UploadResponseDTO;
import com.campustrade.platform.upload.service.UploadService;

import com.campustrade.platform.common.ApiResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/uploads")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UploadResponseDTO> uploadImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok("上传成功", uploadService.storeImage(file));
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> viewImage(@PathVariable String filename) {
        Resource file = uploadService.loadAsResource(filename);
        String contentType = uploadService.detectContentType(filename);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(file);
    }
}

