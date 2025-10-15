package com.boot.backend.food_delivery_app.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    public void sendOrderDeliveredEmail(String toEmail, String customerName, String orderId, List<String> items, double amount) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Your Order #" + orderId + " has been delivered ✅");

            // Thymeleaf template variables
            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("orderId", orderId);
            context.setVariable("orderItems", items);
            context.setVariable("totalAmount", amount);


            String htmlContent = templateEngine.process("order-delivered", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("✅ Email sent to " + toEmail);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send delivery email", e);
        }
    }
    public void sendOrderPlacedEmail(String toEmail, String customerName, String orderId, List<String> items, double amount) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Your Order #" + orderId + " is Placed ✅");

            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("orderId", orderId);
            context.setVariable("orderItems", items);
            context.setVariable("totalAmount", amount);

            String htmlContent = templateEngine.process("order-placed", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("✅ Confirmation email sent to " + toEmail);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send order placed email", e);
        }
    }

}
