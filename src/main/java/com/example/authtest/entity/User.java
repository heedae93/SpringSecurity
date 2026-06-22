package com.example.authtest.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "passwd")
    private String passwd;

    @Column(name = "email")
    private String email;

    @Column(name = "user_type")
    private String userType;
}
