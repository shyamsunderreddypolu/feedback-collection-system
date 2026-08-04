package com.feedback.feedbacksystem.service.impl;

import com.feedback.feedbacksystem.model.AuditLog;
import com.feedback.feedbacksystem.model.User;
import com.feedback.feedbacksystem.repository.AuditLogRepository;
import com.feedback.feedbacksystem.repository.UserRepository;
import com.feedback.feedbacksystem.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    @Async
    @Transactional
    public void logAction(String action, String entityName, Long entityId, Long performedBy, String details) {
        try {
            User user = userRepository.findById(performedBy)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + performedBy));

            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityName(entityName)
                    .entityId(entityId)
                    .performedBy(user)
                    .details(details)
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit log saved: Action '{}' on entity '{}' (id: {}) by user id {}", action, entityName, entityId, performedBy);
        } catch (Exception e) {
            log.error("Failed to record audit log for action '{}' on entity '{}' (id: {}) by user id {}", action, entityName, entityId, performedBy, e);
        }
    }
}
