package com.funnfood.restaurant.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    public void sendReservationConfirmation(
            String to, String name, String restaurant,
            String date, String time, int guests, String token) {

        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Your Reservation Confirmation");

            Context context = new Context();
            Map<String, Object> variables = new HashMap<>();
            variables.put("name", name);
            variables.put("restaurant", restaurant);
            variables.put("date", date);
            variables.put("time", time);
            variables.put("guests", guests);
            variables.put("token", token);
            context.setVariables(variables);

            String htmlContent = templateEngine.process("reservation-confirmation", context);
            helper.setText(htmlContent, true);

            emailSender.send(message);
        } catch (MessagingException e) {
            // Log the error but don't throw exception to prevent reservation process from failing
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    public void sendVerificationEmail(String to, String name, String verificationUrl) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Fun N Food Restaurant - Email Verification");

            Context context = new Context();
            Map<String, Object> variables = new HashMap<>();
            variables.put("name", name);
            variables.put("verificationUrl", verificationUrl);
            context.setVariables(variables);

            String htmlContent = templateEngine.process("email-verification", context);
            helper.setText(htmlContent, true);

            emailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
        }
    }
}
