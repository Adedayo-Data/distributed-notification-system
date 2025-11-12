package com.hng.templateservice.service;

import com.hng.templateservice.dto.NotificationRequestdto;
import com.hng.templateservice.models.NotificationTemplate;
import com.hng.templateservice.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final NotificationTemplateRepository templateRepo;

    // save template
    public NotificationTemplate createTemplate(NotificationRequestdto tempReq){

        NotificationTemplate template = new NotificationTemplate();

        template.setTemplateKey(tempReq.getTemplateKey());
        template.setSubjectTemplate(tempReq.getSubjectTemplate());
        template.setBodyTemplate(tempReq.getBodyTemplate());
        template.setType(tempReq.getType());
        template.setVersion(tempReq.getVersion());

        return templateRepo.save(template);
    }

    // find by templateKey
    public NotificationTemplate findByTemplateKey(String templateKey){

        if (templateKey == null || templateKey.isEmpty()){
            throw new RuntimeException("Template key is not applicable");
        }

        return templateRepo.findByTemplateKey(templateKey);
    }
}
