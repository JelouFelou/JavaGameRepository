package main.java.com.game.model;

import java.util.ArrayList;
import java.util.List;

public class GameData {
    private int currentDay = 1;
    private final List<Character> characters = new ArrayList<>();
    private int foodSupplies = 100;
    private int waterSupplies = 200;
    private int medicineSupplies = 100;

    // Gettery i settery
    public int getCurrentDay() { return currentDay; }
    public void setCurrentDay(int day) { this.currentDay = day; }
    public List<Character> getCharacters() { return characters; }

    public int getFoodSupplies() { return foodSupplies; }
    public void setFoodSupplies(int foodSupplies) { this.foodSupplies = foodSupplies; }

    public int getWaterSupplies() { return waterSupplies; }
    public void setWaterSupplies(int waterSupplies) { this.waterSupplies = waterSupplies; }

    public int getMedicineSupplies() { return medicineSupplies; }
    public void setMedicineSupplies(int medicineSupplies) { this.medicineSupplies = medicineSupplies; }
}