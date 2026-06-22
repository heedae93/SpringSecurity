package com.example.authtest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenDto {

    private String grantType;           // "bearer"
    private String accessToken;
    private Long accessTokenExpiresIn;  // 만료시각 (밀리초)
    private String user_id;
    private String user_type;
}
