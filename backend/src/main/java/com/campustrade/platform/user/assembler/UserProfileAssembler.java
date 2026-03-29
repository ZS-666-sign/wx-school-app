package com.campustrade.platform.user.assembler;

import com.campustrade.platform.upload.service.UploadService;
import com.campustrade.platform.user.dataobject.UserDO;
import com.campustrade.platform.user.dto.response.UserProfileResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class UserProfileAssembler {

    private final UploadService uploadService;

    public UserProfileAssembler(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    public UserProfileResponseDTO toResponse(UserDO user) {
        return new UserProfileResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                uploadService.presignUrl(user.getAvatarUrl())
        );
    }
}

