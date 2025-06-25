package com.hkp.flowershop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private String role;
}


