package main.java.com.game.utils;

import main.java.com.game.model.Event;
import main.java.com.game.model.EventOption;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class EventLoader {
    private static final String EVENT_DIR = "resources/events/";
    private static final String EVENT_EXT = ".txt";
    private static final String OPTIONS_MARKER = "OPTIONS:";

    public static List<Event> loadAllEvents() throws IOException {
        List<Event> events = new ArrayList<>();

        if (!Files.exists(Paths.get(EVENT_DIR))) {
            Files.createDirectories(Paths.get(EVENT_DIR));
            return events;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(EVENT_DIR), "*" + EVENT_EXT)) {
            for (Path file : stream) {
                events.addAll(loadEventsFromFile(file.toString()));
            }
        }

        return events;
    }

    private static List<Event> loadEventsFromFile(String filePath) throws IOException {
        String content = Files.readString(Path.of(filePath));
        content = content.replaceAll("\\r\\n?", "\n");
        return Arrays.stream(content.split("\n---\n"))
                .map(String::trim)
                .filter(block -> !block.isEmpty())
                .map(EventLoader::parseEvent)
                .collect(Collectors.toList());
    }

    private static Event parseEvent(String block) {
        Scanner scanner = new Scanner(block);
        String id = "";
        boolean isUnique = false;
        boolean isHidden = false;
        double frequency = 1.0;
        StringBuilder description = new StringBuilder();
        List<EventOption> options = new ArrayList<>();

        // Parse metadata and description
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();

            if (line.startsWith("#ID:")) {
                id = line.substring(4).trim();
            }
            else if (line.equals("#UNIQUE")) {
                isUnique = true;
            }
            else if (line.equals("#HIDDEN")) {
                isHidden = true;
            }
            else if (line.startsWith("#FREQ:")) {
                frequency = Double.parseDouble(line.substring(6).trim());
            }
            else if (line.equals(OPTIONS_MARKER)) {
                break;
            }
            else if (!line.isEmpty()) {
                description.append(line).append("\n");
            }
        }

        // Parse options
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\|");
            String text = parts[0].trim();
            Map<String, String> effects = new HashMap<>();

            for (int i = 1; i < parts.length; i++) {
                String[] effect = parts[i].trim().split(":");
                String key = effect[0].trim();
                String value = effect.length > 1 ?
                        Arrays.stream(effect).skip(1).collect(Collectors.joining(":")) : "";
                effects.put(key, value);
            }

            options.add(new EventOption(text, effects));
        }

        return new Event(
                id.isEmpty() ? UUID.randomUUID().toString() : id,
                description.toString().trim(),
                options,
                isUnique,
                frequency,
                isHidden
        );
    }
}