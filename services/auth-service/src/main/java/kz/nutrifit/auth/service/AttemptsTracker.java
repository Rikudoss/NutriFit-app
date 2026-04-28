package kz.nutrifit.auth.service;

import kz.nutrifit.auth.entity.EmailVerification;
import kz.nutrifit.auth.repository.EmailVerificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttemptsTracker {

    private final EmailVerificationRepository repository;

    public AttemptsTracker(EmailVerificationRepository repository) {
        this.repository = repository;
    }

    // REQUIRES_NEW: коммитим инкремент независимо от внешней транзакции,
    // чтобы счётчик попыток сохранялся даже когда verifyEmail кидает IllegalArgumentException.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int increment(Long verificationId) {
        EmailVerification v = repository.findById(verificationId)
                .orElseThrow(() -> new IllegalStateException("Verification not found: " + verificationId));
        v.setAttempts(v.getAttempts() + 1);
        repository.save(v);
        return v.getAttempts();
    }
}
