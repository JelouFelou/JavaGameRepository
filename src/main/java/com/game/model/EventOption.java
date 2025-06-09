package main.java.com.game.model;

import java.util.Map;

public class EventOption {
    private final String text;
    private final Map<String, String> effects;

    public EventOption(String text, Map<String, String> effects) {
        this.text = text;
        this.effects = effects;
    }

    // Gettery
    public String getText() { return text; }
    public Map<String, String> getEffects() { return effects; }
}