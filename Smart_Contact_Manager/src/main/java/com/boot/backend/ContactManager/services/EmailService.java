package com.boot.backend.ContactManager.services;

import com.boot.backend.ContactManager.entities.Contact;
import com.boot.backend.ContactManager.entities.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Async
    public void sendBirthdayEmail(User user, Contact contact) {
        try {
            // Create a MIME message (for HTML support)
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(user.getEmail());
            helper.setSubject("🎉 Birthday Reminder for " + contact.getFirstName());

            // Prepare data for template
            Context context = new Context();
            context.setVariable("userName", user.getName());
            context.setVariable("contactName", contact.getFirstName() + " " + contact.getLastName());

            // Process template
            String htmlContent = templateEngine.process("birthday-email", context);
            helper.setText(htmlContent, true);

            // Send email
            mailSender.send(message);

            System.out.println("✅ Birthday email sent to: " + user.getEmail());
        } catch (MessagingException e) {
            System.err.println("❌ Error sending birthday email to " + user.getEmail() + ": " + e.getMessage());
        }
    }
}
