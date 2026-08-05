package com.feedback.feedbacksystem.service;

public interface AuditLogService {

    void logAction(String action, String entityName, Long entityId, Long performedBy, String details);
}
