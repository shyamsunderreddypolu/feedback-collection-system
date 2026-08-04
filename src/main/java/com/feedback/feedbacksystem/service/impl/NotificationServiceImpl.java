package com.feedback.feedbacksystem.service.impl;

import com.feedback.feedbacksystem.dto.NotificationResponseDto;
import com.feedback.feedbacksystem.model.Notification;
import com.feedback.feedbacksystem.model.NotificationPriority;
import com.feedback.feedbacksystem.model.NotificationType;
import com.feedback.feedbacksystem.model.User;
import com.feedback.feedbacksystem.repository.NotificationRepository;
import com.feedback.feedbacksystem.repository.UserRepository;
import com.feedback.feedbacksystem.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public void sendNotification(Long userId, String title, String message, NotificationType type, NotificationPriority priority) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .notificationType(type != null ? type : NotificationType.SYSTEM_ALERT)
                .priority(priority != null ? priority : NotificationPriority.MEDIUM)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getUserNotifications(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(NotificationResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + notificationId));

        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
