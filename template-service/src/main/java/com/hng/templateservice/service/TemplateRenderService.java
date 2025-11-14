package com.hng.templateservice.service;

import com.hng.templateservice.dto.RenderRequestDto;
import com.hng.templateservice.dto.RenderResponseDto;
import com.hng.templateservice.models.NotificationTemplate;
import com.hng.templateservice.repository.NotificationTemplateRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TemplateRenderService {

    private final NotificationTemplateRepository templateRepository;
    private final Logger logger = LoggerFactory.getLogger(TemplateRenderService.class);

    public RenderResponseDto renderTemplate(RenderRequestDto request) {
        logger.info("Rendering template for key: {} and type: {}", request.getTemplateKey(), request.getNotificationType());
        // 1. Fetch the template from the database
        // once you send template code, no need for type

        NotificationTemplate template = templateRepository
                .optFindByTemplateKey(request.getTemplateKey())
                .orElseThrow(() -> new RuntimeException("Template not found for key: "
                        + request.getTemplateKey()));

        // NotificationTemplate template = templateRepository
        //         .findByTemplateKeyAndType(request.getTemplateKey(), request.getNotificationType())
        //         .orElseThrow(() -> new RuntimeException("Template not found for key: "
        //                 + request.getTemplateKey() + " and type: " + request.getNotificationType()));

        String subject = template.getSubjectTemplate();
        String body = template.getBodyTemplate();

        // 2. Loop through the variables and replace placeholders
        if (request.getVariables() != null) {
            for (Map.Entry<String, String> entry : request.getVariables().entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = entry.getValue();

                if (subject != null) {
                    subject = subject.replace(placeholder, value);
                }
                // if (body != null) {
                //     body = body.replace(placeholder, value);
                // }
            }
        }

        // 3. Return the final rendered strings
        return new RenderResponseDto(subject, body);
    }
}