package main.java.com.game.utils;

import main.java.com.game.model.Character;
import main.java.com.game.model.Event;
import main.java.com.game.model.GameData;
import java.util.*;

public class GameBackup {
    private int foodSupplies;
    private int waterSupplies;
    private int medicineSupplies;
    private int currentDay;
    private int lastEventChoice;
    private Event currentDayEvent;
    private Set<String> unlockedEvents;
    private Map<String, String> eventSequences;
    private Map<String, String> statusMessages;
    private List<Character> characters;

    public void backupState(GameData gameData, Event currentDayEvent,
        Set<String> unlockedEvents, Map<String, String> eventSequences,
        Map<String, String> statusMessages, int lastEventChoice) {
        this.foodSupplies = gameData.getFoodSupplies();
        this.waterSupplies = gameData.getWaterSupplies();
        this.medicineSupplies = gameData.getMedicineSupplies();
        this.currentDay = gameData.getCurrentDay();
        this.currentDayEvent = currentDayEvent;
        this.unlockedEvents = new HashSet<>(unlockedEvents);
        this.eventSequences = new HashMap<>(eventSequences);
        this.statusMessages = new HashMap<>(statusMessages);
        this.lastEventChoice = lastEventChoice;

        this.characters = new ArrayList<>();
        for (Character original : gameData.getCharacters()) {
            Character copy = new Character(original.getName());
            copy.setHealth(original.getHealth());
            copy.setHungry(original.isHungry());
            copy.setThirsty(original.isThirsty());
            copy.setSick(original.isSick());
            characters.add(copy);
        }
    }

    public void restoreState(GameData gameData) {
        gameData.setFoodSupplies(foodSupplies);
        gameData.setWaterSupplies(waterSupplies);
        gameData.setMedicineSupplies(medicineSupplies);
        gameData.setCurrentDay(currentDay);

        // Przywróć stan każdej postaci
        for (int i = 0; i < gameData.getCharacters().size(); i++) {
            Character original = gameData.getCharacters().get(i);
            Character backup = characters.get(i);

            original.setHealth(backup.getHealth());
            original.setHungry(backup.isHungry());
            original.setThirsty(backup.isThirsty());
            original.setSick(backup.isSick());
        }
    }

    public Event getBackupCurrentDayEvent() {
        return currentDayEvent;
    }

    public Set<String> getBackupUnlockedEvents() {
        return unlockedEvents;
    }

    public Map<String, String> getBackupEventSequences() {
        return eventSequences;
    }

    public Map<String, String> getBackupStatusMessages() {
        return statusMessages;
    }

    public int getBackupLastEventChoice() {
        return lastEventChoice;
    }
}