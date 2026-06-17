package kz.nutrifit.backend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIChatRequest {
    @com.fasterxml.jackson.annotation.JsonProperty("model")
    private String model;

    @com.fasterxml.jackson.annotation.JsonProperty("messages")
    private List<Message> messages;

    @com.fasterxml.jackson.annotation.JsonProperty("temperature")
    private double temperature;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        @com.fasterxml.jackson.annotation.JsonProperty("role")
        private String role;

        @com.fasterxml.jackson.annotation.JsonProperty("content")
        private String content;
    }
}
