package com.example.authtest.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserDto {

    private String user_id;
    private String passwd;
    private String email;
    private String user_type;

    private List<String> roles = new ArrayList<>();

    public void addRole(String role) {
        this.roles.add(role);
    }
}
