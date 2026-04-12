package kz.nutrifit.backend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AIRequest {
    private String prompt;
}
