package main.java.com.game.utils;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import main.java.com.game.model.Character;

public class StatusLoader {
    private static final String STATUS_DIR = "resources/status/";
    private static final Map<String, List<String>> messagesByState = new HashMap<>();
    private static final Random random = new Random();

    static {
        loadAllStatusFiles();
        ensureNeutralState();
    }

    private static void loadAllStatusFiles() {
        try {
            Path statusDir = Paths.get(STATUS_DIR);
            if (!Files.exists(statusDir)) {
                Files.createDirectories(statusDir);
                return;
            }

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(statusDir, "*.txt")) {
                for (Path file : stream) {
                    String fileName = file.getFileName().toString();
                    String stateKey = fileName.substring(0, fileName.length() - 4); // Remove .txt
                    loadMessagesForState(stateKey, file);
                }
            }
        } catch (IOException e) {
            System.err.println("Błąd ładowania statusów: " + e.getMessage());
        }
    }

    private static void loadMessagesForState(String stateKey, Path file) {
        try {
            List<String> lines = Files.readAllLines(file);
            List<String> filtered = lines.stream()
                    .filter(line -> !line.trim().isEmpty())
                    .collect(Collectors.toList());
            messagesByState.put(stateKey, filtered);
        } catch (IOException e) {
            System.err.println("Błąd ładowania wiadomości dla stanu: " + stateKey);
        }
    }

    private static void ensureNeutralState() {
        if (!messagesByState.containsKey("neutral")) {
            messagesByState.put("neutral", Arrays.asList(
                    "świetnie się czuje",
                    "jest w dobrym nastroju"
            ));
        }
    }

    public static String getRandomMessage(Character gameCharacter) {
        List<String> stateKeys = new ArrayList<>();

        if (gameCharacter.isSick()) stateKeys.add("sick");
        if (gameCharacter.isHungry()) stateKeys.add("hungry");
        if (gameCharacter.isThirsty()) stateKeys.add("thirsty");

        if (stateKeys.isEmpty()) {
            stateKeys.add("neutral");
        }

        // Sortujemy klucze dla spójności (np. sick_hungry zamiast hungry_sick)
        Collections.sort(stateKeys);
        String combinedKey = String.join("_", stateKeys);

        // Szukamy w kolejności: kombinacja -> pojedyncze stany -> neutral
        List<String> keysToTry = new ArrayList<>();
        keysToTry.add(combinedKey);
        keysToTry.addAll(stateKeys);
        keysToTry.add("neutral");

        for (String key : keysToTry) {
            if (messagesByState.containsKey(key)) {
                List<String> messages = messagesByState.get(key);
                return messages.get(random.nextInt(messages.size()));
            }
        }

        return "Brak wiadomości dla aktualnego stanu";
    }
}