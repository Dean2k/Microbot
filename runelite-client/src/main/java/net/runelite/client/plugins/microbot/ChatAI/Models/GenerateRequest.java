package net.runelite.client.plugins.microbot.ChatAI.Models;

import java.util.Map;

// Request DTOs
public class GenerateRequest {
    public String model;
    public String prompt;
    public boolean stream = false;
    public Map<String, Object> options;

    public GenerateRequest(String model, String prompt) {
        this.model = model;
        this.prompt = prompt;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }
}