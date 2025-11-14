package com.hng.pushservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserResponseDto {
    private String id;
    private String name;
    private String email;
    
    @JsonProperty("push_token")
    private String pushToken;
    
    private String password;
    
    private UserPreferences preferences;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    @JsonProperty("updated_at")
    private String updatedAt;
}