package com.tricolori.backend.service;

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
            log.info("Activation email sent to {{}}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send activation email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send activation email", e);
        }
    }

    public void sendDriverRegistrationEmail(String toEmail, String firstName, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Finish Your Driver Registration");

            String activationLink = frontendUrl + "/password-setup?token=" + token;

            String htmlContent = buildDriverRegistrationEmailHtml(firstName, activationLink);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Activation email sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send activation email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send activation email", e);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String firstName, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Reset Your Password - Cuber App");

            String resetLink = frontendUrl + "/reset-password?token=" + token;

            String htmlContent = buildPasswordResetEmailHtml(firstName, resetLink);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    public void sendLinkedPassengerEmail(String toEmail, String firstName, String organizerName,
                                         String from, String to, String scheduledTime, Long rideId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("You've Been Added to a Shared Ride - Cuber App");

            String rideDetailsLink = frontendUrl + "/passenger/ride-details/" + rideId;

            String htmlContent = buildLinkedPassengerEmailHtml(firstName, organizerName, from, to,
                    scheduledTime, rideDetailsLink);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Linked passenger email sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send linked passenger email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send linked passenger email", e);
        }
    }

    public void sendRideReminderEmail(String toEmail, String firstName, int minutesUntilPickup,
                                      String from, String to, Long rideId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(String.format("Ride Reminder: %d Minutes Until Pickup - Cuber App", minutesUntilPickup));

            String trackingLink = frontendUrl + "/passenger/ride-tracking/" + rideId;

            String htmlContent = buildRideReminderEmailHtml(firstName, minutesUntilPickup, from, to, trackingLink);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Ride reminder email sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send ride reminder email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send ride reminder email", e);
        }
    }

    private String buildPasswordResetEmailHtml(String firstName, String resetLink) {
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
                    <h1>Password Reset Request</h1>
                </div>
                <div class="content">
                    <h2>Hi %s,</h2>
                    <p>We received a request to reset the password for your Cuber App account.</p>
                    <p>No worries, it happens! Click the button below to choose a new password:</p>
                    <center>
                        <a href="%s" class="button">Reset My Password</a>
                    </center>
                    <p>Or copy and paste this link into your browser:</p>
                    <p style="word-break: break-all; color: #00acc1;">%s</p>
                    <p>If you didn't request a password reset, you can safely ignore this email. Your password will remain the same.</p>
                    <p><strong>Note:</strong> This link is valid for only 60 minutes for security reasons.</p>
                    <p>Best regards,<br>The Cuber App Team</p>
                </div>
                <div class="footer">
                    <p>This is an automated security email. Please do not reply to this message.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(firstName, resetLink, resetLink);
    }

    private String buildDriverRegistrationEmailHtml(String firstName, String activationLink) {
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
                .role-badge { background: #e0f7fa; color: #00838f; padding: 5px 10px; 
                              border-radius: 4px; font-weight: bold; font-size: 14px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Welcome to the Cuber Team!</h1>
                </div>
                <div class="content">
                    <h2>Hi %s,</h2>
                    <p>Congratulations! You have been successfully registered as a <span class="role-badge">Driver</span>.</p>
                    <p>You are just <strong>one step away</strong> from getting on the road. All you need to do is set up your password to secure your account.</p>
                    <p>Click the button below to choose your password and activate your profile:</p>
                    <center>
                        <a href="%s" class="button">Set Password & Activate Account</a>
                    </center>
                    <p>Or copy and paste this link into your browser:</p>
                    <p style="word-break: break-all; color: #00acc1;">%s</p>
                    <p><strong>Important:</strong> This activation link will expire in 24 hours.</p>
                    <p>We are excited to have you on board!</p>
                    <p>Best regards,<br>The Cuber App Team</p>
                </div>
                <div class="footer">
                    <p>This is an automated email. Please do not reply to this message.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(firstName, activationLink, activationLink);
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

    private String buildLinkedPassengerEmailHtml(String firstName, String organizerName,
                                                 String from, String to, String scheduledTime,
                                                 String rideDetailsLink) {
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
                .ride-details { background: white; padding: 20px; border-radius: 5px; 
                               margin: 20px 0; border-left: 4px solid #00acc1; }
                .detail-row { margin: 10px 0; }
                .detail-label { font-weight: bold; color: #00acc1; }
                .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>🚗 You've Been Added to a Ride!</h1>
                </div>
                <div class="content">
                    <h2>Hi %s,</h2>
                    <p><strong>%s</strong> has added you as a passenger to their upcoming ride.</p>
                    
                    <div class="ride-details">
                        <h3>Ride Details</h3>
                        <div class="detail-row">
                            <span class="detail-label">📍 Pickup Location:</span> %s
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">🎯 Destination:</span> %s
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">🕐 Scheduled Time:</span> %s
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">👥 Organized By:</span> %s
                        </div>
                    </div>
                    
                    <p>You can track your ride and see all the details by clicking the button below:</p>
                    
                    <center>
                        <a href="%s" class="button">View Ride Details</a>
                    </center>
                    
                    <p>Or copy and paste this link into your browser:</p>
                    <p style="word-break: break-all; color: #00acc1;">%s</p>
                    
                    <p>We'll send you another notification when your ride is about to start!</p>
                    
                    <p>Safe travels,<br>The Cuber App Team</p>
                </div>
                <div class="footer">
                    <p>This is an automated email. Please do not reply to this message.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(firstName, organizerName, from, to, scheduledTime, organizerName,
                rideDetailsLink, rideDetailsLink);
    }

    private String buildRideReminderEmailHtml(String firstName, int minutesUntilPickup,
                                              String from, String to, String trackingLink) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #ff9800 0%%, #f57c00 100%%); 
                          color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }
                .button { display: inline-block; padding: 12px 30px; background: #ff9800; 
                          color: white; text-decoration: none; border-radius: 5px; 
                          font-weight: bold; margin: 20px 0; }
                .button:hover { background: #f57c00; }
                .timer { font-size: 48px; font-weight: bold; color: #ff9800; 
                        text-align: center; margin: 20px 0; }
                .ride-info { background: white; padding: 20px; border-radius: 5px; 
                            margin: 20px 0; border-left: 4px solid #ff9800; }
                .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>⏰ Ride Starting Soon!</h1>
                </div>
                <div class="content">
                    <h2>Hi %s,</h2>
                    <p>This is a friendly reminder that your scheduled ride is starting soon!</p>
                    
                    <div class="timer">
                        %d minutes
                    </div>
                    <p style="text-align: center; color: #666; margin-top: -10px;">until pickup</p>
                    
                    <div class="ride-info">
                        <div style="margin: 10px 0;">
                            <strong>📍 Pickup:</strong> %s
                        </div>
                        <div style="margin: 10px 0;">
                            <strong>🎯 Destination:</strong> %s
                        </div>
                    </div>
                    
                    <p><strong>Please be ready at your pickup location.</strong> Your driver will arrive shortly!</p>
                    
                    <p>Track your ride in real-time:</p>
                    
                    <center>
                        <a href="%s" class="button">Track My Ride</a>
                    </center>
                    
                    <p>Or copy and paste this link into your browser:</p>
                    <p style="word-break: break-all; color: #ff9800;">%s</p>
                    
                    <p>Have a great ride!<br>The Cuber App Team</p>
                </div>
                <div class="footer">
                    <p>This is an automated reminder. Please do not reply to this message.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(firstName, minutesUntilPickup, from, to, trackingLink, trackingLink);
    }
}