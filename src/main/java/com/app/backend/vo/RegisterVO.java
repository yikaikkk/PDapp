package com.app.backend.vo;

import lombok.Data;

@Data
public class RegisterVO {
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String phone;
}
