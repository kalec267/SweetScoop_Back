package com.sweetscoop.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private boolean success;
    private String role;
    private String username;
    private String name;
    private Integer branchId;
}