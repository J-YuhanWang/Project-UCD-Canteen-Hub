package io.github.j_yuhanwang.food_ordering_app.email_notification.services;

import io.github.j_yuhanwang.food_ordering_app.email_notification.dtos.NotificationDTO;
import io.github.j_yuhanwang.food_ordering_app.email_notification.entity.Notification;
import io.github.j_yuhanwang.food_ordering_app.email_notification.repository.NotificationRepository;
import io.github.j_yuhanwang.food_ordering_app.enums.NotificationType;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

/**
 * @author YuhanWang
 * @Date 20/02/2026 12:11 pm
 */
@ExtendWith(MockitoExtension.class)
public class NotificationServiceImplTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    @DisplayName("Testing1: Send Email - should successfully send email and save to DB")
    void shouldSendEmailAndSaveToDatabase(){
        //Arrange
        NotificationDTO dto = NotificationDTO.builder()
                .subject("UCD Canteen offer")
                .recipient("id@ucd.ie")
                .body("<h1>Welcome</h1>")
                .notificationType(NotificationType.EMAIL)
                .isHtml(true)
                .build();
        //Stubbing: mock a temporary mimeMessage instance
        MimeMessage mockMimeMessage = mock(MimeMessage.class);
        //1.Given 2.Will 3.Then return
        when(javaMailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        //Act: Polymorphism
        notificationService.sendVerificationEmail(dto);

        //Assert
        verify(javaMailSender,times(1)).send(mockMimeMessage);
        verify(notificationRepository,times(1)).save(any(Notification.class));

    }
}
