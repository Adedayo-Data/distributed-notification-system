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

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;
    private final TemplateRenderService renderService;

    // create template
    @PostMapping
    public ResponseEntity<NotificationTemplate> createTemplate(@RequestBody NotificationRequestdto req){

        NotificationTemplate template = templateService.createTemplate(req);
        if (template == null){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

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

        try {
            // 1. Try to render the template
            RenderResponseDto renderedData = renderService.renderTemplate(request);

            // 2. Wrap in a SUCCESS response (meta is null, as we discussed)
            ApiResponseDto<RenderResponseDto> response =
                    new ApiResponseDto<>(true, "Template rendered successfully", renderedData, null);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 3. Wrap in a FAILURE response
            ApiResponseDto<Object> errorResponse =
                    new ApiResponseDto<>(false, "Template rendering failed", e.getMessage(), null);

            // Return 404 if not found, 400 for bad request, etc.
            // For now, 400 is a good general error.
            return ResponseEntity.status(400).body(errorResponse);
        }
    }
}
