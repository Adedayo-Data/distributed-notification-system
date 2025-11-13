package com.hng.templateservice.repository;

import com.hng.templateservice.models.NotificationTemplate;
import com.hng.templateservice.models.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

    NotificationTemplate findByTemplateKey(String templateKey);

    Optional<NotificationTemplate> findByTemplateKeyAndType(String templateKey, NotificationType type);
}
