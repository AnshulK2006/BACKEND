package com.cvns.service;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.cvns.custom_exceptions.ApiException;
import com.cvns.dtos.ResponseDtos.ChatResponse;

@Service
public class ChatbotService {
    private final RestClient client;

    public ChatbotService(RestClient.Builder builder, @Value("${services.chatbot.base-url}") String url) {
        client = builder.baseUrl(url).build();
    }

    public ChatResponse ask(String message) {
        try {
            Map<?, ?> response = client.post().uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(Map.of("message", message))
                    .retrieve()
                    .body(Map.class);
            return new ChatResponse(String.valueOf(response.get("answer")),
                    Boolean.TRUE.equals(response.get("vaccination_related")));
        } catch (Exception e) {
            throw new ApiException("Groq chatbot is unavailable. Check GROQ_API_KEY and the Python chatbot service.");
        }
    }
}
