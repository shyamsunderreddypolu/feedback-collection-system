package com.feedback.feedbacksystem.service;

import com.feedback.feedbacksystem.dto.NotificationResponseDto;
import com.feedback.feedbacksystem.model.NotificationPriority;
import com.feedback.feedbacksystem.model.NotificationType;

import java.util.List;

public interface NotificationService {

    void sendNotification(Long userId, String title, String message, NotificationType type, NotificationPriority priority);

    List<NotificationResponseDto> getUserNotifications(Long userId);

    void markAsRead(Long notificationId);
}
