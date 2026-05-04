package kz.nutrifit.user.entity;

import jakarta.persistence.*;
import kz.nutrifit.user.enums.ActivityLevel;
import kz.nutrifit.user.enums.Gender;
import kz.nutrifit.user.enums.Goal;
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

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    private String fullName;
    private Integer age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private Double heightCm;
    private Double weightKg;

    @Enumerated(EnumType.STRING)
    private Goal goal;

    @Enumerated(EnumType.STRING)
    private ActivityLevel activityLevel;

    @Builder.Default
    @Column(nullable = false)
    private boolean onboardingCompleted = false;
}
