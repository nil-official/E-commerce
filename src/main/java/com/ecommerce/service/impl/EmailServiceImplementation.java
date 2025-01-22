package com.ecommerce.service.impl;

import com.ecommerce.exception.EmailSendingException;
import com.ecommerce.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
@AllArgsConstructor
public class EmailServiceImplementation implements EmailService {

    private final JavaMailSender mailSender;

    @Async
    @Override
    public void sendVerificationEmail(String toEmail, String verifyLink) throws MessagingException {
        // Create email content
        String subject = "Email Verification";
//        String content = "<p>Please verify your email by clicking the link below:</p>"
//                + "<a href=\"" + verifyLink + "\">Verify Email</a>";

        // New content
        String content =
                "<!DOCTYPE html>\n" +
                        "<html lang=\"en\">\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                        "    <title>Email Verification</title>\n" +
                        "    <style>\n" +
                        "        body {\n" +
                        "            font-family: Arial, sans-serif;\n" +
                        "            background-color: #f9f9f9;\n" +
                        "            margin: 0;\n" +
                        "            padding: 0;\n" +
                        "        }\n" +
                        "        .email-container {\n" +
                        "            max-width: 600px;\n" +
                        "            margin: 40px auto;\n" +
                        "            background-color: #ffffff;\n" +
                        "            padding: 30px;\n" +
                        "            border-radius: 10px;\n" +
                        "            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);\n" +
                        "            text-align: center;\n" +
                        "        }\n" +
                        "        .logo {\n" +
                        "            font-size: 28px;\n" +
                        "            font-weight: bold;\n" +
                        "            color: #000000;\n" +
                        "            margin-bottom: 20px;\n" +
                        "        }\n" +
                        "        .content {\n" +
                        "            font-size: 16px;\n" +
                        "            color: #333333;\n" +
                        "            line-height: 1.8;\n" +
                        "            margin-bottom: 20px;\n" +
                        "        }\n" +
                        "        .verify-button {\n" +
                        "            display: inline-block;\n" +
                        "            padding: 12px 25px;\n" +
                        "            font-size: 16px;\n" +
                        "            font-weight: bold;\n" +
                        "            color: #ffffff !important;\n" +
                        "            background-color: #000000;\n" +
                        "            text-decoration: none;\n" +
                        "            border-radius: 5px;\n" +
                        "        }\n" +
                        "        .verify-button:hover {\n" +
                        "            background-color: #333333;\n" +
                        "        }\n" +
                        "        .footer {\n" +
                        "            font-size: 12px;\n" +
                        "            color: #888888;\n" +
                        "            margin-top: 20px;\n" +
                        "        }\n" +
                        "    </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <div class=\"email-container\">\n" +
                        "        <div class=\"logo\">Stitch</div>\n" +
                        "        <div class=\"content\">\n" +
                        "            <p>Please verify your email by clicking the button below:</p>\n" +
                        "            <a href=\"" + verifyLink + "\" class=\"verify-button\">Verify Email</a>\n" +
                        "        </div>\n" +
                        "        <div class=\"footer\">\n" +
                        "            If you did not request this email, you can safely ignore it.\n" +
                        "        </div>\n" +
                        "    </div>\n" +
                        "</body>\n" +
                        "</html>\n";

        // Send the email
        sendEmail(toEmail, subject, content);
    }

    @Async
    @Override
    public void sendResetPasswordEmail(String toEmail, String resetLink) throws MessagingException {
        // Create email content
        String subject = "Reset Your Password";
//        String content = "<p>You have requested to reset your password. Click the link below to reset it:</p>"
//                + "<a href=\"" + resetLink + "\">Reset Password</a>"
//                + "<p><b>Note:</b> This link is valid for 1 hour only.</p>";

        // New content
        String content =
                "<!DOCTYPE html>\n" +
                        "<html lang=\"en\">\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                        "    <title>Email Verification</title>\n" +
                        "    <style>\n" +
                        "        body {\n" +
                        "            font-family: Arial, sans-serif;\n" +
                        "            background-color: #f9f9f9;\n" +
                        "            margin: 0;\n" +
                        "            padding: 0;\n" +
                        "        }\n" +
                        "        .email-container {\n" +
                        "            max-width: 600px;\n" +
                        "            margin: 40px auto;\n" +
                        "            background-color: #ffffff;\n" +
                        "            padding: 30px;\n" +
                        "            border-radius: 10px;\n" +
                        "            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);\n" +
                        "            text-align: center;\n" +
                        "        }\n" +
                        "        .logo {\n" +
                        "            font-size: 28px;\n" +
                        "            font-weight: bold;\n" +
                        "            color: #000000;\n" +
                        "            margin-bottom: 20px;\n" +
                        "        }\n" +
                        "        .content {\n" +
                        "            font-size: 16px;\n" +
                        "            color: #333333;\n" +
                        "            line-height: 1.8;\n" +
                        "            margin-bottom: 20px;\n" +
                        "        }\n" +
                        "        .verify-button {\n" +
                        "            display: inline-block;\n" +
                        "            padding: 12px 25px;\n" +
                        "            font-size: 16px;\n" +
                        "            font-weight: bold;\n" +
                        "            color: #ffffff !important;\n" +
                        "            background-color: #000000;\n" +
                        "            text-decoration: none;\n" +
                        "            border-radius: 5px;\n" +
                        "        }\n" +
                        "        .verify-button:hover {\n" +
                        "            background-color: #333333;\n" +
                        "        }\n" +
                        "        .footer {\n" +
                        "            font-size: 12px;\n" +
                        "            color: #888888;\n" +
                        "            margin-top: 20px;\n" +
                        "        }\n" +
                        "    </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <div class=\"email-container\">\n" +
                        "        <div class=\"logo\">Stitch</div>\n" +
                        "        <div class=\"content\">\n" +
                        "            <p>You have requested to reset your password. Click the link below to reset it:</p>\n" +
                        "            <a href=\"" + resetLink + "\" class=\"verify-button\">Reset Password</a>\n" +
                        "            <p><b>Note:</b>This link is valid for 1 hour only.</p>\n" +
                        "        </div>\n" +
                        "        <div class=\"footer\">\n" +
                        "            If you did not request this email, you can safely ignore it.\n" +
                        "        </div>\n" +
                        "    </div>\n" +
                        "</body>\n" +
                        "</html>\n";


        // Send the email
        sendEmail(toEmail, subject, content);
    }

    private void sendEmail(String to, String subject, String content) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        try {
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // Set content as HTML
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailSendingException("Failed to send email to " + to, e);
        }
    }

}
