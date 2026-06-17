package kz.nutrifit.backend.profile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import kz.nutrifit.backend.auth.enums.Gender;
import kz.nutrifit.backend.auth.enums.Goal;
import kz.nutrifit.backend.user.User;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private Integer age;

    @Enumerated(EnumType.STRING)          // ✅ enum в БД как текст
    private Gender gender;

    private Double heightCm;
    private Double weightKg;

    @Enumerated(EnumType.STRING)          // ✅ enum в БД как текст
    private Goal goal;

    @Builder.Default                       // ✅ чтобы builder не сбивал дефолт
    @Column(nullable = false)
    private boolean onboardingCompleted = false;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;
}
