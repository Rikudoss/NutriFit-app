package kz.nutrifit.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;
    private final String from;

    public MailService(JavaMailSender mailSender,
                       @Value("${app.mail.from:noreply@nutrifit.kz}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendVerificationEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("NutriFit AI — Email Verification Code");
        message.setText(buildBody(code));

        mailSender.send(message);
        log.info("Verification email sent to {}", to);
    }

    private String buildBody(String code) {
        return """
                Hello!

                Your verification code: %s

                The code expires in 10 minutes.

                If you didn't request this, please ignore this email.

                — NutriFit AI Team
                """.formatted(code);
    }
}
