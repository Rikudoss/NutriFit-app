package kz.nutrifit.backend.profile;

import kz.nutrifit.backend.auth.enums.Gender;
import kz.nutrifit.backend.auth.enums.Goal;
import lombok.Data;


    @Data
    public class ProfilePatchRequest {
        private String fullName;
        private Integer age;
        private Gender gender;
        private Double heightCm;
        private Double weightKg;   // ТОЛЬКО КГ
        private Goal goal;
    }

