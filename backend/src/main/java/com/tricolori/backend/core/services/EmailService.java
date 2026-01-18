package com.tricolori.backend.core.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void sendActivationEmail(String toEmail, String firstName, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Activate Your Taxi App Account");

            String activationLink = frontendUrl + "/activate?token=" + token;

            String htmlContent = buildActivationEmailHtml(firstName, activationLink);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Activation email sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send activation email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send activation email", e);
        }
    }

    private String buildActivationEmailHtml(String firstName, String activationLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #00acc1 0%%, #0097a7 100%%); 
                              color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; padding: 12px 30px; background: #00acc1; 
                              color: white; text-decoration: none; border-radius: 5px; 
                              font-weight: bold; margin: 20px 0; }
                    .button:hover { background: #008ba3; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to Cuber App!</h1>
                    </div>
                    <div class="content">
                        <h2>Hi %s,</h2>
                        <p>Thank you for registering with Taxi App!</p>
                        <p>To complete your registration and activate your account, please click the button below:</p>
                        <center>
                            <a href="%s" class="button">Activate My Account</a>
                        </center>
                        <p>Or copy and paste this link into your browser:</p>
                        <p style="word-break: break-all; color: #00acc1;">%s</p>
                        <p><strong>Important:</strong> This link will expire in 24 hours.</p>
                        <p>If you didn't create an account with us, please ignore this email.</p>
                        <p>Best regards,<br>The Cuber App Team</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated email. Please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(firstName, activationLink, activationLink);
    }
}