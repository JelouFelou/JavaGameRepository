package main.java.com.game.model;

import java.util.List;

public class Event {
    private final String id;
    private final String description;
    private final List<EventOption> options;
    private final boolean isUnique;
    private final double frequency;

    public Event(String id, String description, List<EventOption> options, boolean isUnique, double frequency) {
        this.id = id;
        this.description = description;
        this.options = options;
        this.isUnique = isUnique;
        this.frequency = frequency;
    }

    // Gettery
    public String getId() { return id; }
    public String getDescription() { return description; }
    public List<EventOption> getOptions() { return options; }
    public boolean isUnique() { return isUnique; }
    public double getFrequency() { return frequency; }
}