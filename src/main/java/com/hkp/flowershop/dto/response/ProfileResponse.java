package com.hkp.flowershop.dto.response;

import com.hkp.flowershop.enums.Role;
import com.hkp.flowershop.model.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@Builder
public class ProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String profileImageUrl;
    private String address;
    private Role role;
    private LocalDateTime createdAt;

    public static ProfileResponse from(User user) {
        Objects.requireNonNull(user, "user must not be null");
        return ProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profileImageUrl(user.getProfileImageUrl())
                .address(user.getAddress())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
