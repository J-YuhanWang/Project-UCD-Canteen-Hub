package io.github.j_yuhanwang.food_ordering_app.email_notification.services;

import io.github.j_yuhanwang.food_ordering_app.email_notification.dtos.NotificationDTO;
import io.github.j_yuhanwang.food_ordering_app.email_notification.entity.Notification;
import io.github.j_yuhanwang.food_ordering_app.email_notification.repository.NotificationRepository;
import io.github.j_yuhanwang.food_ordering_app.exceptions.EmailDeliveryException;
import io.github.j_yuhanwang.food_ordering_app.order.dtos.OrderDTO;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

/**
 * @author YuhanWang
 * @Date 19/02/2026 8:55 pm
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService{
    private final JavaMailSender javaMailSender;
    private final NotificationRepository notificationRepository;

    private final TemplateEngine templateEngine;

    @Async
    @Override
    public void sendVerificationEmail(NotificationDTO notificationDTO) {
        log.info("Inside sendVerificationEmail()");
        try{
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            helper.setTo(notificationDTO.getRecipient());
            helper.setSubject(notificationDTO.getSubject());
            helper.setText(notificationDTO.getBody(), notificationDTO.isHtml());
            javaMailSender.send(mimeMessage);

            //save to database
            Notification notification = Notification.builder()
                    .recipient(notificationDTO.getRecipient())
                    .subject(notificationDTO.getSubject())
                    .body(notificationDTO.getBody())
                    .notificationType(notificationDTO.getNotificationType())
                    .isHtml(notificationDTO.isHtml())
                    .build();

            notificationRepository.save(notification);
            log.info("Email record saved to database.");
        }catch (Exception e){
            log.error("Failed to send email:{}",e.getMessage());
            throw new EmailDeliveryException("Failed to send notification email to " + notificationDTO.getRecipient());
        }
    }

//    @Async
//    public void sendOrderConfirmation(OrderDTO orderDTO){
//        log.info("Preparing order confirmation email for order: {}", orderDTO.getId());
//        Context context = new Context();
//        context.setVariable("customerName",orderDTO.getUserName());
//        context.setVariable("orderId",orderDTO.getId());
//        context.setVariable("orderDate",orderDTO.getOrderDate());
//        context.setVariable("orderId",orderDTO.getId());
//    }
}
