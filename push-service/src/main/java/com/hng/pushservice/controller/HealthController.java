package com.hng.pushservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> checkHealthStatus(){
        Map<String, String> response = new LinkedHashMap<>();
        response.put("Status", "UP");
        response.put("Service", "push-service");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
