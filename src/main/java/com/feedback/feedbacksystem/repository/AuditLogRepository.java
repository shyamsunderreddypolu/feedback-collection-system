package com.feedback.feedbacksystem.repository;

import com.feedback.feedbacksystem.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

   List<AuditLog> findByPerformedById(Long userId);

    List<AuditLog> findByEntityNameAndEntityId(String entityName, Long entityId);

    List<AuditLog> findByPerformedAtBetween(LocalDateTime start, LocalDateTime end);

    List<AuditLog> findByPerformedByIdOrderByPerformedAtDesc(Long performedBy);
}
