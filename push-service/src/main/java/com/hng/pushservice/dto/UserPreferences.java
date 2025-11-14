package com.hng.pushservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UserPreferences {

    @JsonProperty("push_notifications") 
    private Boolean push;

    @JsonProperty("email_notifications") 
    private Boolean email;
}
