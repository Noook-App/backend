package com.personalspace.api.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.personalspace.api.dto.request.AiRecommendRequest;
import com.personalspace.api.dto.response.AiRecommendResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class AiService {

    private final RestClient restClient;
    private final String model;
    private final String completionsUrl;

    public AiService(
            @Value("${ai.api-key}") String apiKey,
            @Value("${ai.base-url}") String baseUrl,
            @Value("${ai.model}") String model) {
        this.model = model;
        this.completionsUrl = baseUrl.stripTrailing() + "/chat/completions";
        this.restClient = RestClient.builder()
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public AiRecommendResponse recommend(AiRecommendRequest request) {
        String systemPrompt = buildSystemPrompt(request.type());
        String userMessage = buildUserMessage(request);

        ChatCompletionRequest chatRequest = new ChatCompletionRequest(
                model,
                List.of(
                        new ChatMessage("system", systemPrompt),
                        new ChatMessage("user", userMessage)
                )
        );

        ChatCompletionResponse response = restClient.post()
                .uri(completionsUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(chatRequest)
                .retrieve()
                .body(ChatCompletionResponse.class);

        String suggestion = response.choices().get(0).message().content();
        return new AiRecommendResponse(suggestion.trim());
    }

    private String buildSystemPrompt(String type) {
        return switch (type) {
            case "note" -> "You are a helpful writing assistant. Based on the note content provided, suggest " +
                    "improved or expanded content. Return ONLY valid HTML using <p>, <strong>, <em>, " +
                    "<ul>/<li>, <ol>/<li> tags. Do not include <html>, <head>, or <body> tags, " +
                    "and do not wrap in markdown code blocks.";
            case "todo" -> "You are a helpful productivity assistant. Suggest todo items based on the context. " +
                    "Return ONLY a JSON array of strings with no explanation or markdown code blocks. " +
                    "Example: [\"Buy milk\", \"Call dentist\", \"Finish report\"]";
            case "grocery" -> "You are a helpful shopping assistant. Suggest grocery items based on the context. " +
                    "Return ONLY a JSON array of objects with \"name\" and optional \"quantity\" string fields, " +
                    "no explanation, no markdown code blocks. " +
                    "Example: [{\"name\":\"Milk\",\"quantity\":\"1 gallon\"},{\"name\":\"Eggs\",\"quantity\":\"12\"}]";
            default -> "You are a helpful assistant.";
        };
    }

    private String buildUserMessage(AiRecommendRequest request) {
        StringBuilder sb = new StringBuilder();
        if (request.title() != null && !request.title().isBlank()) {
            sb.append("Title: ").append(request.title()).append("\n");
        }
        if (request.currentContent() != null && !request.currentContent().isBlank()) {
            sb.append("Current content:\n").append(request.currentContent()).append("\n");
        }
        if (request.context() != null && !request.context().isBlank()) {
            sb.append("User request: ").append(request.context());
        }
        if (sb.isEmpty()) {
            sb.append("Please provide helpful suggestions.");
        }
        return sb.toString();
    }

    record ChatCompletionRequest(String model, List<ChatMessage> messages) {}

    record ChatMessage(String role, String content) {}

    record ChatCompletionResponse(List<Choice> choices) {}

    record Choice(ChatMessage message, @JsonProperty("finish_reason") String finishReason) {}
}
