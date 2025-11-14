package com.hng.templateservice.controller;

import com.hng.templateservice.dto.ApiResponseDto;
import com.hng.templateservice.dto.NotificationRequestdto;
import com.hng.templateservice.dto.RenderRequestDto;
import com.hng.templateservice.dto.RenderResponseDto;
import com.hng.templateservice.models.NotificationTemplate;
import com.hng.templateservice.service.TemplateRenderService;
import com.hng.templateservice.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;
    private final TemplateRenderService renderService;
    private final Logger logger = LoggerFactory.getLogger(TemplateController.class);

    @PostMapping
    public ResponseEntity<NotificationTemplate> createTemplate(@RequestBody NotificationRequestdto req){

        NotificationTemplate template = templateService.createTemplate(req);

//        if (template == null){
//            // Ensure error response is also in the ApiResponseDto format, even on failure
//            ApiResponseDto<Object> errorResponse = new ApiResponseDto<>(
//                    false, "Template creation failed", "Internal service error during save.", null);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body((ApiResponseDto)errorResponse);
//        }
//
//        // Wrap the successful NotificationTemplate object in the ApiResponseDto
//        ApiResponseDto<NotificationTemplate> response = new ApiResponseDto<>(
//                true, "Template created successfully", template, null);

        return ResponseEntity.status(HttpStatus.OK).body(template);
    }

    // Get by Template key
    @GetMapping("/{templateKey}")
    public ResponseEntity<NotificationTemplate> getTemplateByTemplateKey(@PathVariable String templateKey){

        NotificationTemplate template = templateService.findByTemplateKey(templateKey);

        if (template == null){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(template);
    }

    @PostMapping("/render")
public ResponseEntity<ApiResponseDto<?>> renderTemplate(@RequestBody RenderRequestDto request) {
    logger.info("Received render request for template key: {}", request.getTemplateKey());
    try {
        // 1. Try to render the template
        RenderResponseDto renderedData = renderService.renderTemplate(request);
        logger.info("Template rendered successfully for key: {}", request.getTemplateKey());

        // 2. Use the .success() method
        ApiResponseDto<RenderResponseDto> response = ApiResponseDto.success(
                "Template rendered successfully", renderedData, null);

        logger.info("Response: {}", response); // This will now show success=true
        return ResponseEntity.ok(response);

    } catch (Exception e) {
        // 3. Use the .fail() method
        ApiResponseDto<Object> errorResponse = ApiResponseDto.fail(
                "Template rendering failed", e.getMessage(), null);
        
        return ResponseEntity.status(400).body(errorResponse);
    }
}
}
