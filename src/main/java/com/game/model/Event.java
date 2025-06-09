package main.java.com.game.model;

import java.util.List;

public class Event {
    private final String id;
    private final String description;
    private final List<EventOption> options;
    private final boolean isUnique;
    private final boolean isHidden;
    private final double frequency;

    public Event(String id, String description, List<EventOption> options, boolean isUnique, double frequency, boolean isHidden) {
        this.id = id;
        this.description = description;
        this.options = options;
        this.isUnique = isUnique;
        this.isHidden = isHidden;
        this.frequency = frequency;
    }

    // Gettery
    public String getId() { return id; }
    public String getDescription() { return description; }
    public List<EventOption> getOptions() { return options; }
    public boolean isUnique() { return isUnique; }
    public boolean isHidden() { return isHidden; }
    public double getFrequency() { return frequency; }
}