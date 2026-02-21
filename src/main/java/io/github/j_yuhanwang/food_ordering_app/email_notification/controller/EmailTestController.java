package io.github.j_yuhanwang.food_ordering_app.email_notification.controller;

import io.github.j_yuhanwang.food_ordering_app.email_notification.dtos.NotificationDTO;
import io.github.j_yuhanwang.food_ordering_app.email_notification.services.NotificationService;
import io.github.j_yuhanwang.food_ordering_app.enums.NotificationType;
import io.github.j_yuhanwang.food_ordering_app.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Restricts this controller to the 'dev' profile.
 * Used for manual smoke testing via Postman.
 *
 * @author YuhanWang
 * @Date 19/02/2026 10:18 pm
 */
//Lombok
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/test")
@Profile("dev") // Ensures this debug tool is not loaded in production to maintain security
public class EmailTestController {
    private final NotificationService notificationService;

    @PostMapping("/send-email")
    public Response<String> testSendEmail(@RequestParam String email){
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(email)
                .subject("UCD Canteen - Test Email")
                .body("<h1>Hello from Spring Boot!</h1><p>If you see this, your SMTP configuration is perfect.</p>")
                .notificationType(NotificationType.EMAIL)
                .isHtml(true)
                .build();

        notificationService.sendVerificationEmail(notificationDTO);
        return Response.ok("Email dispatch triggered for: " + email);
    }
}
