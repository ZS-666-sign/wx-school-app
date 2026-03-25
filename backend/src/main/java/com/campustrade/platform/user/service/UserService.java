package com.campustrade.platform.user.service;

import com.campustrade.platform.common.AppException;
import com.campustrade.platform.user.dataobject.UserDO;
import com.campustrade.platform.user.dto.request.UpdateProfileRequestDTO;
import com.campustrade.platform.user.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public UserDO getById(Long userId) {
        UserDO user = userMapper.findById(userId);
        if (user == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    @Transactional
    public UserDO updateProfile(Long userId, UpdateProfileRequestDTO request) {
        getById(userId);
        userMapper.updateProfile(
                userId,
                request.nickname().trim(),
                request.avatarUrl() == null ? null : request.avatarUrl().trim());
        return getById(userId);
    }
}
