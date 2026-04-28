package kz.nutrifit.auth.service;

import kz.nutrifit.auth.entity.EmailVerification;
import kz.nutrifit.auth.entity.User;
import kz.nutrifit.auth.repository.EmailVerificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class EmailVerificationService {

    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmailVerificationRepository verificationRepository;
    private final StringRedisTemplate redisTemplate;
    private final MailService mailService;
    private final AttemptsTracker attemptsTracker;

    public EmailVerificationService(EmailVerificationRepository verificationRepository,
                                    StringRedisTemplate redisTemplate,
                                    MailService mailService,
                                    AttemptsTracker attemptsTracker) {
        this.verificationRepository = verificationRepository;
        this.redisTemplate = redisTemplate;
        this.mailService = mailService;
        this.attemptsTracker = attemptsTracker;
    }

    @Transactional
    public void generateAndSendCode(User user) {
        verificationRepository.deleteByUserId(user.getId());

        String code = generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plus(CODE_TTL);

        EmailVerification verification = EmailVerification.builder()
                .userId(user.getId())
                .code(code)
                .expiresAt(expiresAt)
                .attempts(0)
                .build();
        verificationRepository.save(verification);

        redisTemplate.opsForValue().set(codeKey(user.getId()), code, CODE_TTL);

        mailService.sendVerificationEmail(user.getEmail(), code);
        log.info("Verification code generated for userId={}", user.getId());
    }

    @Transactional
    public boolean verifyCode(Long userId, String code) {
        String redisCode = redisTemplate.opsForValue().get(codeKey(userId));
        if (redisCode != null && redisCode.equals(code)) {
            markUsed(userId);
            redisTemplate.delete(codeKey(userId));
            return true;
        }

        Optional<EmailVerification> activeOpt =
                verificationRepository.findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(userId);
        if (activeOpt.isEmpty()) {
            return false;
        }
        EmailVerification active = activeOpt.get();

        if (active.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Блокировка по исчерпанию попыток должна работать и через БД-путь
        // (после удаления Redis-ключа). Иначе после 5 неудач можно ввести
        // правильный код через DB-fallback.
        if (active.getAttempts() >= MAX_ATTEMPTS) {
            return false;
        }

        if (active.getCode().equals(code)) {
            active.setUsedAt(LocalDateTime.now());
            verificationRepository.save(active);
            redisTemplate.delete(codeKey(userId));
            return true;
        }

        // Инкремент идёт через REQUIRES_NEW — коммитится независимо
        // от внешней транзакции AuthService.verifyEmail (которая откатится
        // при IllegalArgumentException на неверный код).
        int newAttempts = attemptsTracker.increment(active.getId());

        if (newAttempts >= MAX_ATTEMPTS) {
            redisTemplate.delete(codeKey(userId));
            log.warn("Code blocked after {} attempts for userId={}", MAX_ATTEMPTS, userId);
        }
        return false;
    }

    @Transactional
    public void resendCode(User user) {
        String resendKey = resendKey(user.getId());
        Boolean firstHit = redisTemplate.opsForValue()
                .setIfAbsent(resendKey, "1", RESEND_COOLDOWN);
        if (Boolean.FALSE.equals(firstHit)) {
            throw new IllegalStateException("Wait before requesting new code");
        }
        generateAndSendCode(user);
    }

    private void markUsed(Long userId) {
        verificationRepository
                .findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(userId)
                .ifPresent(v -> {
                    v.setUsedAt(LocalDateTime.now());
                    verificationRepository.save(v);
                });
    }

    private static String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private static String codeKey(Long userId) {
        return "verification:user:" + userId;
    }

    private static String resendKey(Long userId) {
        return "verification:resend:user:" + userId;
    }
}
