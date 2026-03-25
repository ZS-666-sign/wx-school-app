package com.campustrade.platform.user.dataobject;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserDO {

    private Long id;
    private String email;
    private String passwordHash;
    private String nickname;
    private String avatarUrl;
    private Integer failedLoginCount = 0;
    private LocalDateTime lockedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
