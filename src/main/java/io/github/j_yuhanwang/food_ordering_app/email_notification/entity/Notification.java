package io.github.j_yuhanwang.food_ordering_app.email_notification.entity;

import io.github.j_yuhanwang.food_ordering_app.enums.NotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Represents a log of a notification (email, SMS, etc.) sent by the system.
 * <p>
 * This entity is primarily used for audit trails, debugging delivery issues,
 * and maintaining a history of communication with users.
 *
 * @author BlairWang
 * @version 1.0
 * @date 26/01/2026 8:43 am
 */

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="notifications")
@Data
@EntityListeners(AuditingEntityListener.class)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "recipient is required")
    @Email(message = "Invalid email format")//email validation
    private String recipient;

    /**
     * The main content of the notification.
     * <p>
     * Annotated with {@code @Lob} (Large Object) to support storing large HTML templates
     * or extensive text that exceeds standard database column limits.
     */
    @Lob
    private String body;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    /**
     * Indicates whether the body content is HTML (true) or plain text (false).
     * Used by the email service to set the correct MIME type.
     */
    private boolean isHtml;
}
