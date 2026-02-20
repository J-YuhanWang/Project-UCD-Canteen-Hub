package io.github.j_yuhanwang.food_ordering_app.email_notification.services;

import io.github.j_yuhanwang.food_ordering_app.email_notification.dtos.NotificationDTO;

/**
 * @author YuhanWang
 * @Date 19/02/2026 8:52 pm
 */
public interface NotificationService {
    void sendVerificationEmail(NotificationDTO notificationDTO);
}
