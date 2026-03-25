package com.campustrade.platform.user.assembler;

import com.campustrade.platform.user.dataobject.UserDO;
import com.campustrade.platform.user.dto.response.UserProfileResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class UserProfileAssembler {

    public UserProfileResponseDTO toResponse(UserDO user) {
        return new UserProfileResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getAvatarUrl()
        );
    }
}

