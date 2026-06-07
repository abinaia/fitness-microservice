package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Service
public class GeminiService {
    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.model}")
    private String geminiModel;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Autowired
    public GeminiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String getAnswer(String question) {
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", question)
                        })
                }
        );

        try {
            // Validate configuration
            if (geminiApiUrl == null || geminiApiUrl.isEmpty()) {
                throw new RuntimeException("GEMINI_API_URL is not configured");
            }
            if (geminiModel == null || geminiModel.isEmpty()) {
                throw new RuntimeException("GEMINI_MODEL is not configured");
            }
            if (geminiApiKey == null || geminiApiKey.isEmpty()) {
                throw new RuntimeException("GEMINI_API_KEY is not configured");
            }

            // Build the complete endpoint URL
            // Expected format: https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=...
            String uri = geminiApiUrl + "/" + geminiModel + ":generateContent?key=" + geminiApiKey;

            logger.debug("Base URL: {}", geminiApiUrl);
            logger.debug("Model: {}", geminiModel);
            logger.debug("API Key length: {}", geminiApiKey.length());

            logger.info("Final URI (key hidden): {}", uri.replaceAll("key=.*", "key=***"));
            logger.info("Request Body: {}", objectMapper.writeValueAsString(requestBody));

            String response = webClient.post()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            logger.info("API Response received successfully");
            logger.debug("Raw Response: {}", response);

            // Parse the Gemini response and extract the text answer
            JsonNode rootNode = objectMapper.readTree(response);
            if (rootNode.has("candidates") && rootNode.get("candidates").isArray() &&
                !rootNode.get("candidates").isEmpty()) {
                JsonNode firstCandidate = rootNode.get("candidates").get(0);
                if (firstCandidate.has("content") &&
                    firstCandidate.get("content").has("parts") &&
                    firstCandidate.get("content").get("parts").isArray() &&
                    !firstCandidate.get("content").get("parts").isEmpty()) {
                    return firstCandidate.get("content").get("parts").get(0).get("text").asText();
                }
            }
            return response;
        } catch (WebClientResponseException e) {
            logger.error("Gemini API HTTP Error: Status={}, Body={}",
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Gemini API returned error " + e.getStatusCode() +
                ": " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Failed to get answer from Gemini API", e);
            throw new RuntimeException("Failed to get answer from Gemini API: " + e.getMessage(), e);
        }
    }
}