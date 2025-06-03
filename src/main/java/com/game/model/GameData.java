package main.java.com.game.model;

import java.util.ArrayList;
import java.util.List;

public class GameData {
    private int currentDay = 1;
    private final List<Character> characters = new ArrayList<>();
    private int foodSupplies = 3;

    // Gettery i settery
    public int getCurrentDay() { return currentDay; }
    public void setCurrentDay(int day) { this.currentDay = day; }
    public List<Character> getCharacters() { return characters; }
    public int getFoodSupplies() { return foodSupplies; }
    public void setFoodSupplies(int amount) { this.foodSupplies = amount; }
}