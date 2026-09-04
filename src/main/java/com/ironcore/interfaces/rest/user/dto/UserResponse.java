package com.ironcore.interfaces.rest.user.dto;

public record UserResponse(
        Long userId,
        String email,
        String nickname,
        Boolean mustChangePassword
) {
}
