package io.github.j_yuhanwang.food_ordering_app.email_notification.repository;

import io.github.j_yuhanwang.food_ordering_app.email_notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for notification entities.
 *
 * @author YuhanWang
 * @Date 28/01/2026 6:15 pm
 */
public interface NotificationRepository extends JpaRepository<Notification,Long>{
}
