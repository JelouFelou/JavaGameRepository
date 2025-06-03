package main.java.com.game.utils;

import main.java.com.game.model.Character;
import main.java.com.game.model.GameData;
import java.util.ArrayList;
import java.util.List;

public class GameBackup {
    private int backupFoodState;

    public List<Character> backupCharacters(GameData gameData) {
        backupFoodState = gameData.getFoodSupplies();
        List<Character> backup = new ArrayList<>();
        for(Character original : gameData.getCharacters()) {
            Character copy = new Character(original.getName());
            copy.setHungry(original.isHungry());
            backup.add(copy);
        }
        return backup;
    }

    public void restoreCharacters(GameData gameData, List<Character> backup) {
        gameData.setFoodSupplies(backupFoodState);
        for(int i = 0; i < gameData.getCharacters().size(); i++) {
            gameData.getCharacters().get(i).setHungry(backup.get(i).isHungry());
        }
    }
}
