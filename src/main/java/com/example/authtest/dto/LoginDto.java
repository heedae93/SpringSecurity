package com.example.authtest.dto;

import lombok.Data;

@Data
public class LoginDto {

    private String id;
    private String passwd;
    private String user_type;   // U: 일반사용자, A: 관리자
}
