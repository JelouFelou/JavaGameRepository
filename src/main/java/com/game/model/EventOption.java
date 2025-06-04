package main.java.com.game.model;

import java.util.Map;

public class EventOption {
    private final String text;
    private final Map<String, Integer> effects;

    public EventOption(String text, Map<String, Integer> effects) {
        this.text = text;
        this.effects = effects;
    }

    // Gettery
    public String getText() { return text; }
    public Map<String, Integer> getEffects() { return effects; }
}