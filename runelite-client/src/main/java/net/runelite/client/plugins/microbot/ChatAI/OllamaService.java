package net.runelite.client.plugins.microbot.ChatAI;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.runelite.client.plugins.microbot.ChatAI.Models.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


public class OllamaService {
    //private static final String OLLAMA_BASE_URL = "http://192.168.1.115:11434";
    private final HttpClient client;
    private final ObjectMapper mapper;

    public OllamaService() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public String generateText(String model, String prompt, String ip) throws Exception {
        GenerateRequest request = new GenerateRequest(model, prompt);
        String jsonBody = mapper.writeValueAsString(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(ip + "/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            GenerateResponse generateResponse = mapper.readValue(response.body(), GenerateResponse.class);
            return generateResponse.response;
        } else {
            throw new RuntimeException("Generation failed: " + response.statusCode());
        }
    }

    public String chat(String model, String userMessage, String ip) throws Exception {
        List<Message> messages = List.of(new Message("user", userMessage), new Message("system", "Give short response."));
        ChatRequest request = new ChatRequest(model, messages);
        String jsonBody = mapper.writeValueAsString(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(ip + "/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            ChatResponse chatResponse = mapper.readValue(response.body(), ChatResponse.class);
            return chatResponse.message.content;
        } else {
            throw new RuntimeException("Chat failed: " + response.statusCode());
        }
    }
}
