package kz.nutrifit.backend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIRequest {
    private String prompt;
    private Boolean withPersonalData; // false by default if null

    public AIRequest(String prompt) {
        this.prompt = prompt;
    }
}
