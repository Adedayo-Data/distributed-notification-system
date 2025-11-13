package com.hng.pushservice.dto;

import lombok.Data;

@Data
public class UserResponseDto {

    private String pushToken;
    private UserPreferences preferences;
}
