package kz.nutrifit.backend.ai.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class OpenAIChatResponse {
    private List<Choice> choices;

    @Data
    @NoArgsConstructor
    public static class Choice {
        private OpenAIChatRequest.Message message;
    }
}
